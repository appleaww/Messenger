package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.model.dto.TypingDTO;
import io.github.appleaww.messenger.model.dto.request.MessageCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.request.ReadReceiptRequestDTO;
import io.github.appleaww.messenger.model.dto.response.MessageCreateResponseDTO;
import io.github.appleaww.messenger.model.dto.response.ReadReceiptResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.MessageRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public MessageCreateResponseDTO createMessage(MessageCreateRequestDTO messageCreateRequestDTO, User user) {

        User sender = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + user.getId()));

        Chat chat = chatRepository.findById(messageCreateRequestDTO.chatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id " + messageCreateRequestDTO.chatId()));
        if (!chat.getParticipants().contains(sender)) {
            throw new IllegalArgumentException("Chat with id " + messageCreateRequestDTO.chatId() + "does not contain user with id " + sender.getId());
        }

        User recipient = chat.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

        Message message = new Message();
        message.setContent(messageCreateRequestDTO.content());
        message.setSender(sender);
        message.setSendingTime(Instant.now());
        message.setChat(chat);

        chat.getMessages().add(message);

        log.debug("Message saved in chat with id {} by User with id {}",message.getId(), sender.getId());

        return new MessageCreateResponseDTO(
                message.getId(),
                message.getSendingTime(),
                message.getContent(),
                message.isRead(),
                sender.getId(),
                recipient.getId(),
                chat.getId()
        );

    }

    @Transactional
    public ReadReceiptResponseDTO processReadReceipt(ReadReceiptRequestDTO readReceiptRequestDTO, User user){
        User reader = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + user.getId()));
        Chat chat = chatRepository.findById(readReceiptRequestDTO.chatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id " + readReceiptRequestDTO.chatId()));

        if(!chat.getParticipants().contains(reader)){
            throw new IllegalArgumentException("Chat with id " + readReceiptRequestDTO.chatId() + "does not contain user with id " + reader.getId());
        }

        User recipient = chat.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(reader.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

        List<Message> messagesToUpdate = messageRepository.findAllById(readReceiptRequestDTO.messageIds());

        messagesToUpdate.stream()
                .filter(message -> !message.getSender().equals(reader))
                .forEach(message -> message.setRead(true));

        log.debug("Read receipt processed for chat {}", chat.getId());

        return new ReadReceiptResponseDTO(
                chat.getId(),
                readReceiptRequestDTO.messageIds(),
                reader.getId(),
                recipient.getId()
        );
    }

    @Transactional
    public TypingDTO processTyping(TypingDTO typingDTO, User user){
        User typingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + user.getId()));
        Chat chat = chatRepository.findById(typingDTO.chatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found with id " + typingDTO.chatId()));

        if(!chat.getParticipants().contains(typingUser)){
            throw new IllegalArgumentException("Chat with id " + typingDTO.chatId() + "does not contain user with id " + typingUser.getId());
        }

        User recipient = chat.getParticipants().stream()
                .filter(participant -> !participant.getId().equals(typingUser.getId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Recipient not found"));

        log.debug("Typing processed for chat {} by user {}", typingDTO.chatId(), user.getId());

        return new TypingDTO(
                chat.getId(),
                typingUser.getId(),
                typingUser.getUsername(),
                recipient.getId(),
                true
        );

    }
}
