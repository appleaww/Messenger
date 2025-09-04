package io.github.appleaww.messenger.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatCreateRequestDTO(
        @NotBlank(message = "Companion username cannot be empty")
        String companionUsername
) {}
