package io.github.appleaww.messenger.kafka.metrics.event;

import java.time.LocalDateTime;

public record BusinessEvent(
        String type,
        String userId,
        Long sessionDurationMs,
        LocalDateTime timestamp
) {}
