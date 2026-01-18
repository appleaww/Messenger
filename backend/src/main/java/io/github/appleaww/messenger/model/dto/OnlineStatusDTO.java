package io.github.appleaww.messenger.model.dto;

import java.time.Instant;

public record OnlineStatusDTO(
        Long userId,
        boolean isOnline,
        Instant lastSeen
) {}
