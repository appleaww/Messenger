package io.github.appleaww.messenger.model.dto;

import java.time.Instant;

public record MessageDTO(Long id,
                         String content,
                         Instant sendingTime,
                         Long senderId,
                         String senderUsername,
                         String senderName,
                         boolean isRead,
                         boolean isMine) {
}
