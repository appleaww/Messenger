package io.github.appleaww.messenger.mapper;

import io.github.appleaww.messenger.model.dto.response.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.ParticipantDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    public ChatCreateResponseDTO toDTO(Chat chat, User companion){
        return new ChatCreateResponseDTO(
                chat.getId(),
                chat.getLastMessage(),
                companion.getName(),
                chat.getParticipants().stream().map(user -> new ParticipantDTO(user.getId(), user.getUsername())).collect(Collectors.toList())
        );
    }
}
