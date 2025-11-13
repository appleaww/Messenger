package io.github.appleaww.messenger.model.dto;

import java.time.Instant;

public record TypingDTO(
        Long chatId,
        Long userId,
        String username,
        Long recipientId,
        boolean isTyping) {
}
