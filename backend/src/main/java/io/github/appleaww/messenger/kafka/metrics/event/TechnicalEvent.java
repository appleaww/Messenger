package io.github.appleaww.messenger.kafka.metrics.event;

import java.time.LocalDateTime;

public record TechnicalEvent(
        String type,
        String userId,
        Long latencyMs,
        Double throughput,
        Double cpuUsage,
        Double memoryUsedBytes,
        LocalDateTime timestamp
)
{}
