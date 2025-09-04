package io.github.appleaww.messenger.mapper;

import io.github.appleaww.messenger.model.dto.MessageCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.MessageCreateResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MessageMapper {
    public Message toEntity(MessageCreateRequestDTO messageCreateRequestDTO, User sender, Chat chat){
        Message message = new Message();
        message.setSendingTime(Instant.now());
        message.setContent(messageCreateRequestDTO.content());
        message.setChat(chat);
        message.setSender(sender);
        return message;
    }

    public MessageCreateResponseDTO toDTO(Message message){
        return new MessageCreateResponseDTO(
                 message.getId(),
                 message.getSendingTime(),
                 message.getContent(),
                 message.isRead(),
                 message.getSender().getId(),
                 message.getChat().getId()
        );
    }
}
