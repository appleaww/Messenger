package io.github.appleaww.messenger.mapper;

import io.github.appleaww.messenger.model.dto.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.ParticipantDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMapper {
    public ChatCreateResponseDTO toDTO(Chat chat, String initiatorUsername){
        User currentUser = chat.getParticipants().stream()
                .filter(user -> user.getUsername().equals(initiatorUsername))
                .findFirst().orElseThrow(RuntimeException::new);
        String chatName = currentUser.getName();

        return new ChatCreateResponseDTO(
                chat.getId(),
                chatName,
                chat.getParticipants().stream().map(user -> new ParticipantDTO(user.getId(), user.getUsername())).collect(Collectors.toList())
        );
    }
}
