package io.github.appleaww.messenger.metrics.event;

import java.time.Instant;

public record UserActivityEvent(String userId,
                                String actionType,
                                Instant timestamp
) {}
