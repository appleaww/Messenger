package io.github.appleaww.messenger.model.dto.response;

import java.util.List;

public record ReadReceiptResponseDTO(
        Long chatId,
        List<Long> messageIds,
        Long readerId,
        Long recipientId)
{}
