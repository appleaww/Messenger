package io.github.appleaww.messenger.model.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record MessageCreateResponseDTO(
        Long id,
        Instant sendingTime,
        String content,
        boolean isRead,
        Long senderId,
        Long chatId
) {}

