package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.mapper.ChatMapper;
import io.github.appleaww.messenger.model.dto.request.ChatCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.response.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.ChatDetailDTO;
import io.github.appleaww.messenger.model.dto.ChatListItemDTO;
import io.github.appleaww.messenger.model.dto.MessageDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
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

    @Transactional
    public ChatCreateResponseDTO createChat(ChatCreateRequestDTO chatCreateRequestDTO, User initiator) {
        User companion = userRepository.findByUsername(chatCreateRequestDTO.companionUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found with username " + chatCreateRequestDTO.companionUsername()));

        if (initiator.getId().equals(companion.getId())) {
            throw new RuntimeException("Cannot create chat with yourself");
        }
        Optional<Chat> existingChat = chatRepository.findExistingChatBetweenUserIds(
                initiator.getId(), companion.getId()
        );

        if (existingChat.isPresent()) {
            throw new RuntimeException("Chat already exists");
        }
        Chat chat = new Chat();
        chat.setParticipants(Set.of(initiator, companion));
        chat.getChatNames().put(initiator.getId(), companion.getName());
        chat.getChatNames().put(companion.getId(), initiator.getName());
        chat.setLastMessage("Send the first message!");

        chatRepository.save(chat);

        log.debug("Chat created with id {}", chat.getId());
        return chatMapper.toDTO(chat, companion);

    }
    
    @Transactional
    public List<ChatListItemDTO> closeCertainChat(Long chatId, User user) {
        saveLastMessage(chatId);
        log.debug("User with id {} closed chat", user.getId());
        return getAllUserChatsWithDetails(user);
    }

    @Transactional
    public void saveLastMessage(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id " + chatId));
        String lastMessage = chat.getMessages().stream()
                .max(Comparator.comparing(Message::getSendingTime))
                .map(Message::getContent)
                .orElse("Send the first message!");

        chat.setLastMessage(lastMessage);

        log.debug("last message created for chat with id {}", chatId);
    }

    @Transactional
    public List<ChatListItemDTO> getAllUserChatsWithDetails(User user) {
        User refreshedUser = userRepository.findWithChatsAndMessagesById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User with id " + user.getId() + " not found"));

        log.debug("User with id {} requested all chats", user.getId());

        return refreshedUser.getChats().stream()
                .map(chat -> {
                    User companion = chat.getParticipants().stream()
                            .filter(participant -> !participant.getId().equals(user.getId()))
                            .findFirst().orElse(null);

                    Optional<Message> lastMessage = chat.getMessages().stream()
                            .max(Comparator.comparing(Message::getSendingTime));

                    String lastMessageContent = lastMessage.map(Message::getContent)
                            .orElse(chat.getLastMessage());

                    Instant lastMessageSendingTime = lastMessage.map(Message::getSendingTime)
                            .orElse(Instant.MIN);

                    Long unreadMessagesCount = chat.getMessages().stream()
                            .filter(message -> !message.getSender().getId().equals(user.getId()))
                            .filter(message -> !message.isRead())
                            .count();

                    return new ChatListItemDTO(
                            chat.getId(),
                            companion != null ? companion.getId() : null,
                            companion != null ? companion.getName() : "Unknown",
                            lastMessageContent,
                            lastMessageSendingTime,
                            unreadMessagesCount
                    );
                }).sorted(Comparator.comparing(ChatListItemDTO::lastMessageSendingTime).reversed()).collect(Collectors.toList());

    }
    @Transactional
    public List<ChatListItemDTO> deleteChat(Long chatId, User user){
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat with id " + chatId + " not found"));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(user.getId()));

        if(!isParticipant){
            throw new IllegalArgumentException("User with id " + user.getId() + " is not a participant of chat with id " + chat.getId());
        }

        chatRepository.delete(chat);

        log.debug("Chat with id {} deleted by User with id {}", chat.getId(), user.getId());
        return getAllUserChatsWithDetails(user);
    }
    @Transactional
    public ChatDetailDTO openChat(Long chatId, User user){
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id " + chatId));
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(user.getId()));

        if(!isParticipant){
            throw new IllegalArgumentException("User with id " + user.getId() + " is not a participant of chat with id " + chat.getId());
        }

        User companion = chat.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(user.getId())).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Companion not found in chat with id " +chat.getId()));

        chat.getMessages().stream().filter(message -> !message.getSender().getId().equals(user.getId()))
                .filter(message -> !message.isRead())
                .forEach(message -> message.setRead(true));

        List<MessageDTO> messages = chat.getMessages().stream()
                .map(message -> new MessageDTO(
                        message.getId(),
                        message.getContent(),
                        message.getSendingTime(),
                        message.getSender().getId(),
                        message.getSender().getUsername(),
                        message.getSender().getName(),
                        message.isRead(),
                        message.getSender().getId().equals(user.getId())
                )).toList();

        log.debug("Chat with id {} opened by User with id {}", chat.getId(), user.getId());
        return new ChatDetailDTO(chat.getId(),
                companion.getName(),
                companion.getUsername(),
                messages);

    }
}
