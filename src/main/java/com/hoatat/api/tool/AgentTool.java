package com.hoatat.api.tool;

import com.hoatat.api.dto.ResourceType;

import java.util.Map;

public interface AgentTool {
    String getName();

    String getDescription();

    ResourceType getResource();

    Object execute(Map<String,Object> params, Map<String,Object> context);
}
