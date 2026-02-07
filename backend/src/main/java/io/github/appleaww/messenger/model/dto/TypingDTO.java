package io.github.appleaww.messenger.model.dto;


public record TypingDTO(
        Long chatId,
        Long userId,
        String username,
        Long recipientId,
        boolean isTyping) {
}
