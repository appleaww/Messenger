package io.github.appleaww.messenger.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageCreateRequestDTO(
        @NotBlank(message = "Content cannot be empty")
        String content,
        @NotNull(message = "Chat ID cannot be null")
        Long chatId
) {}


