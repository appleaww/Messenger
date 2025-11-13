package io.github.appleaww.messenger.model.dto;

import java.util.List;

public record ChatDetailDTO(Long chatId,
                            String companionName,
                            String companionUsername,
                            List<MessageDTO> messages) {
}
