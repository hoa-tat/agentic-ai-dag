package com.hoatat.api.service;

import com.hoatat.api.dto.*;
import com.hoatat.api.exception.CircuitOpenException;
import com.hoatat.api.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutor {
    private final ToolExecutor toolExecutor;
    private final ExecutorService executorService;
    private final CircuitBreakerService circuitBreakerService;
    private final ToolRegistry registry;

    /*public String execute(WorkflowPlan workflowPlan) {
        ExecutionContext context = new ExecutionContext();

        Set<String> completed = new HashSet<>();

        while (completed.size() < workflowPlan.getNodes().size()) {
            List<WorkflowNode> executableNodes =
                    workflowPlan.getNodes()
                            .stream()
                            .filter(node -> !completed.contains(node.getId()))
                            .filter(node ->
                                    dependenciesSatisfied(node, completed))
                            .toList();

            if(executableNodes.isEmpty()) {
                throw new RuntimeException("Deadlock detected");
            }

            for(WorkflowNode node : executableNodes) {
                log.info("Executing node {}", node.getId());

                Object result = toolExecutor.execute(
                                node.getAction(),
                                node.getParams(),
                                context.getAll()
                        );

                context.put(node.getId(), result);

                completed.add(node.getId());
            }
        }

        Object finalAnswer = context.get("final");

        return finalAnswer == null ? context.getAll().toString() : finalAnswer.toString();
    }*/

    public String execute(WorkflowPlan workflowPlan) {
        ExecutionContext context = new ExecutionContext();

        // Khởi tạo trạng thái PENDING cho tất cả các nodes
        initNodeStatus(workflowPlan, context);

        while (!allFinished(context)) {
            markSkippedNodes(workflowPlan, context);

            List<WorkflowNode> readyNodes = findReadyNodes(workflowPlan, context);

            if(readyNodes.isEmpty() && !allFinished(context)) {
                throw new RuntimeException("Deadlock detected");
            }
            // chạy song song
            runParallel(readyNodes, context);
        }

        Object finalAnswer = context.get("final");

        return finalAnswer == null ? context.getAll().toString() : finalAnswer.toString();
    }

    private void runParallel(List<WorkflowNode> nodes, ExecutionContext context) {
        List<CompletableFuture<Void>> futures =
                nodes.stream()
                        .map(node ->
                                CompletableFuture.runAsync(
                                        () -> executeWithRetry(node, context),
                                        executorService
                                )
                        )
                        .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private Object executeWithRetry(WorkflowNode node, ExecutionContext context) {
        log.info("executeWithRetry {}", node.getId());
        context.getStatus().put(node.getId(), NodeStatus.RUNNING);
        int retry = 0;

        AgentTool tool = registry.getTool(node.getAction());

        CircuitBreaker circuitBreaker = circuitBreakerService.get(tool.getResource());

        while (retry <= node.getMaxRetries()) {
            try {
                if (circuitBreaker.getState() == CircuitState.OPEN) {
                    if (System.currentTimeMillis() < circuitBreaker.getOpenUntil()) {
                        log.warn("Circuit breaker is OPEN for tool {}, skipping execution", node.getAction());
                        throw new CircuitOpenException("Circuit breaker is OPEN for tool " + node.getAction());
                    } else {
                        log.info("Circuit breaker timeout expired for tool {}, switching to HALF_OPEN", node.getAction());
                        circuitBreaker.setState(CircuitState.HALF_OPEN);
                    }
                }

                Object result = executeWithTimeout(node, context);

                context.put(node.getId(), result);

                context.getStatus().put(node.getId(), NodeStatus.SUCCESS);

                circuitBreaker.getFailureCount().set(0);
                circuitBreaker.setState(CircuitState.CLOSED);

                return result;
            } catch (TimeoutException ex) {
                log.error("Node {} timeout", node.getId());
                context.getStatus().put(node.getId(), NodeStatus.TIMEOUT);
                return null;
            } catch (CircuitOpenException ex) {
                log.warn("Circuit OPEN for {}", node.getId());
                context.getStatus().put(node.getId(), NodeStatus.CIRCUIT_OPEN);
                return null;
            } catch (Exception ex) {
                // Kiểm tra nếu node thất bại do lỗi công cụ, cập nhật circuit breaker
                int failures = circuitBreaker.getFailureCount().incrementAndGet();
                if (failures >= circuitBreaker.getFailureThreshold()) {
                    circuitBreaker.setState(CircuitState.OPEN);
                    circuitBreaker.setOpenUntil(System.currentTimeMillis() + 30000); // Mở circuit breaker trong 30 giây
                    log.warn("Circuit breaker HALF_OPEN for tool {} due to repeated failures", node.getAction());
                }

                retry++;
                if (retry > node.getMaxRetries()) {
                    log.error("Node {} failed", node.getId(), ex);
                    context.getStatus().put(node.getId(), NodeStatus.FAILED);
                    throw new RuntimeException(ex);
                }

                log.warn("Retry {}/{} for node {}", retry, node.getMaxRetries(), node.getId());
                context.getStatus().put(node.getId(), NodeStatus.RETRYING);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ignored);
                }
            }
        }

        throw new RuntimeException("Unexpected state");
    }

    private Object executeWithTimeout(WorkflowNode node, ExecutionContext context) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("executeWithTimeout {}", node.getId());

        Future<Object> future = executorService.submit(() ->
                toolExecutor.execute(
                        node.getAction(),
                        node.getParams(),
                        context.getAll()
                )
        );
        try {
            return future.get(node.getTimeoutSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            future.cancel(true);
            log.error("Node {} failed or timed out", node.getId(), ex);
            throw ex;
        }
    }

    /*private void executeNode(WorkflowNode node, ExecutionContext context) {
        try {
            log.info("Executing {}", node.getId());
            context.getStatus().put(node.getId(), NodeStatus.RUNNING);

            Object result = toolExecutor.execute(
                            node.getAction(),
                            node.getParams(),
                            context.getAll()
                    );

            context.put(node.getId(), result);

            context.getStatus().put(node.getId(), NodeStatus.SUCCESS);

        } catch (Exception ex) {
            log.error("Node {} failed", node.getId(), ex);
            context.getStatus().put(node.getId(), NodeStatus.FAILED);
        }
    }*/


    private List<WorkflowNode> findReadyNodes(WorkflowPlan plan, ExecutionContext context) {
        return plan.getNodes()
                .stream()
                .filter(node -> context.getStatus().get(node.getId()) == NodeStatus.PENDING)
                .filter(node -> dependenciesSatisfied(node, context))
                .toList();
    }

    private void markSkippedNodes(WorkflowPlan plan, ExecutionContext context) {
        for(WorkflowNode node : plan.getNodes()) {
            if(context.getStatus().get(node.getId()) != NodeStatus.PENDING) {
                continue;
            }

            if(dependencyFailed(node, context)) {
                log.warn("Node {} skipped", node.getId());
                context.getStatus().put(node.getId(), NodeStatus.SKIPPED);
            }
        }
    }

    private boolean allFinished(ExecutionContext context) {
        return context.getStatus()
                .values()
                .stream()
                .allMatch(status ->
                        status == NodeStatus.SUCCESS
                        || status == NodeStatus.FAILED
                        || status == NodeStatus.SKIPPED
                        || status == NodeStatus.TIMEOUT
                );
    }

    private void initNodeStatus(WorkflowPlan plan, ExecutionContext context) {
        for(WorkflowNode node : plan.getNodes()) {
            context.getStatus().put(node.getId(), NodeStatus.PENDING);
        }
    }

    private boolean dependencyFailed(WorkflowNode node, ExecutionContext context) {
        if(node.getDependsOn() == null || node.getDependsOn().isEmpty()) {
            return false;
        }

        return node.getDependsOn()
                .stream()
                .anyMatch(dep -> {
                            NodeStatus status = context.getStatus().get(dep);
                            return status == NodeStatus.FAILED
                                    || status == NodeStatus.TIMEOUT;
                        }
                );
    }

    /*private boolean dependenciesSatisfied(WorkflowNode node, Set<String> completed) {
        if(node.getDependsOn() == null) {
            return true;
        }

        return completed.containsAll(node.getDependsOn());
    }*/

    private boolean dependenciesSatisfied(WorkflowNode node, ExecutionContext context) {
        if(node.getDependsOn() == null || node.getDependsOn().isEmpty()) {
            return true;
        }

        return node.getDependsOn()
                .stream()
                .allMatch(dep ->
                        context.getStatus().get(dep) == NodeStatus.SUCCESS);
    }
}
