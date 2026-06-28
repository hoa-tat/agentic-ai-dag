package com.hoatat.api.controller;

import com.hoatat.api.dto.WorkflowPlan;
import com.hoatat.api.service.PlannerService;
import com.hoatat.api.service.WorkflowExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/agent")
public class AgentController {
    private final PlannerService plannerService;
    private final WorkflowExecutor workflowExecutor;

    @GetMapping
    public String runAgent(@RequestParam String query) {
        WorkflowPlan plan = plannerService.createPlan(query);

        return workflowExecutor.execute(plan);
    }
}
