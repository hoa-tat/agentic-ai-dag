package com.hoatat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowNode {
    private String id;

    private String action;

    private Map<String,Object> params;

    private List<String> dependsOn;

    private int maxRetries = 3;

    private long timeoutSeconds = 30;

    public WorkflowNode(
            String id,
            String action,
            Map<String,Object> params,
            List<String> dependsOn
    ) {
        this.id = id;
        this.action = action;
        this.params = params;
        this.dependsOn = dependsOn;
    }
}
