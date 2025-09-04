package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.mapper.MessageMapper;
import io.github.appleaww.messenger.model.dto.MessageCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.MessageCreateResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.MessageRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper messageMapper;

    @Transactional
    public MessageCreateResponseDTO createMessage(MessageCreateRequestDTO messageCreateRequestDTO) {
        //получение аутентифицированного пользователя
        String username = "   ";
        User sender = userRepository.findByUsername(username).orElseThrow(RuntimeException::new);

        Chat chat = chatRepository.findById(messageCreateRequestDTO.chatId()).orElseThrow(RuntimeException::new);

        if (!chat.getParticipants().contains(sender)) {
            throw new RuntimeException();
        }

        Message message = messageMapper.toEntity(messageCreateRequestDTO, sender, chat);
        Message savedMessage = messageRepository.save(message);

        return messageMapper.toDTO(savedMessage);
    }

}
