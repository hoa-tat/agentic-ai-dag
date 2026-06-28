package com.hoatat.api.service;

import com.hoatat.api.dto.CircuitBreaker;
import com.hoatat.api.dto.ResourceType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CircuitBreakerService {

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public CircuitBreaker get(ResourceType resource) {
        return circuitBreakers.computeIfAbsent(resource.name(), k -> new CircuitBreaker());
    }

    /*public CircuitBreaker get(String toolName) {
        return circuitBreakers.computeIfAbsent(toolName, k -> new CircuitBreaker());
    }*/
}
