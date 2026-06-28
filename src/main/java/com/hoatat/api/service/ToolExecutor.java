package com.hoatat.api.service;

import com.hoatat.api.tool.AgentTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolExecutor {
    private final ToolRegistry registry;

    public Object execute(String toolName, Map<String,Object> params, Map<String,Object> context) {
        log.info("ToolName: {}", toolName);
        AgentTool tool = registry.getTool(toolName);

        if(tool == null) {
            throw new RuntimeException("Tool not found: " + toolName);
        }

        return tool.execute(params, context);
    }
}
