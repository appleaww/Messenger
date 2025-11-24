package io.github.appleaww.messenger.model.dto;

import java.time.Instant;

public record ChatListItemDTO (Long chatId,
                               String companionName,
                               String lastMessage,
                               Instant lastMessageSendingTime,
                               Long unreadMessagesCount){}
