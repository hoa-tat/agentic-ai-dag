package com.hoatat.api.service;

import com.hoatat.api.tool.AgentTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();

    public ToolRegistry(List<AgentTool> toolList) {
        for (AgentTool tool : toolList) {
            tools.put(tool.getName(), tool);
            log.info("Tool registry initialized" + tool.getName());
        }

    }

    public AgentTool getTool(String name) {
        return tools.get(name);
    }
}
