package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.mapper.ChatMapper;
import io.github.appleaww.messenger.model.dto.ChatCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.ParticipantDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    public List<Chat> getAllChats(){
        return (chatRepository.findAll());
    }

    public Optional<Chat> getChatById(Long id){
        return chatRepository.findById(id);
    }

    @Transactional
    public ChatCreateResponseDTO createChat(ChatCreateRequestDTO chatCreateRequestDTO){
        // получаем текущего авторизированного пользователя
        String initiatorUsername = "  ";

        User initiator = userRepository.findByUsername(initiatorUsername).orElseThrow(RuntimeException::new);
        User companion = userRepository.findByUsername(chatCreateRequestDTO.companionUsername()).orElseThrow(RuntimeException::new);

        if(initiator.getId().equals(companion.getId())){
            throw new RuntimeException("Cannot create chat with yourself");
        }
        Optional<Chat> existingChat = findExistingChat(initiator, companion);

        if(existingChat.isPresent()){
            throw new RuntimeException("Chat already exists");
        }
        Chat chat = new Chat();
        chat.setParticipants(Set.of(initiator,companion));

        chat.getChatNames().put(initiator,companion.getName());
        chat.getChatNames().put(companion,initiator.getName());

        chatRepository.save(chat);

        initiator.getChats().add(chat);
        companion.getChats().add(chat);

        return chatMapper.toDTO(chat, initiator.getUsername());

    }
    private Optional<Chat> findExistingChat(User firsUser, User secondUser){
        Set<Long> firstUserIdChats = firsUser.getChats().stream().map(Chat::getId).collect(Collectors.toSet());
        return secondUser.getChats().stream().filter(chat -> firstUserIdChats.contains(chat.getId())).findFirst();
    }

}
