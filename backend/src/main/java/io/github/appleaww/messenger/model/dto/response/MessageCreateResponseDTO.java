package io.github.appleaww.messenger.model.dto.response;

import java.time.Instant;

public record MessageCreateResponseDTO(
        Long messageId,
        Instant sendingTime,
        String content,
        boolean isRead,
        Long senderId,
        Long recipientId,
        Long chatId
) {}

