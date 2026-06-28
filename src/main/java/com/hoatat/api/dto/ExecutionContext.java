package com.hoatat.api.dto;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class ExecutionContext {

    private final Map<String,Object> results = new ConcurrentHashMap<>();

    // bo sung node status cho ExecutionContext
    private final Map<String, NodeStatus> status = new ConcurrentHashMap<>();

    public void put(String nodeId,Object result) {
        results.put(nodeId,result);
    }

    public Object get(String nodeId) {
        return results.get(nodeId);
    }

    public Map<String,Object> getAll() {
        return results;
    }
}
