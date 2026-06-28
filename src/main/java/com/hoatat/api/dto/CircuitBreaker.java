package com.hoatat.api.dto;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class CircuitBreaker {
    private CircuitState state = CircuitState.CLOSED;

    private AtomicInteger failureCount = new AtomicInteger(0);

    private int failureThreshold = 5;

    private long openUntil = 0;

}
