package io.github.appleaww.messenger.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReadReceiptRequestDTO(
        @NotNull(message = "Chat ID is required")
        Long chatId,
        @NotNull(message = "Message IDs list cannot be empty")
        List<Long> messageIds)
{}