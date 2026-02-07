package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.kafka.KafkaProducerService;
import io.github.appleaww.messenger.kafka.metrics.event.TechnicalEvent;
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
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    @Mock private MessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private KafkaProducerService kafkaProducerService;
    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @InjectMocks private MessageService messageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(messageService, "meterRegistry", meterRegistry);
    }

    @Test
    @DisplayName("Test successful message creation")
    void createMessage() {
        Long senderId = 1L;
        Long recipientId = 2L;
        Long chatId = 100L;
        String content = "Hello";

        User sender = new User();
        sender.setId(senderId);

        User recipient = new User();
        recipient.setId(recipientId);

        Set<User> participants = new HashSet<>();
        participants.add(sender);
        participants.add(recipient);

        Chat chat = new Chat();
        chat.setParticipants(participants);
        chat.setId(chatId);

        MessageCreateRequestDTO dto = new MessageCreateRequestDTO(content, chatId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        Message savedMessage = new Message();
        savedMessage.setId(3L);
        savedMessage.setSender(sender);
        savedMessage.setChat(chat);
        savedMessage.setContent(content);
        savedMessage.setRead(false);
        savedMessage.setSendingTime(Instant.now());

        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        doNothing().when(kafkaProducerService).sendMessage(anyString(), anyString(), any(TechnicalEvent.class));

        MessageCreateResponseDTO result = messageService.createMessage(dto, sender);

        assertThat(result)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.messageId()).isEqualTo(3L);
                    assertThat(res.chatId()).isEqualTo(chatId);
                    assertThat(res.sendingTime()).isNotNull();
                    assertThat(res.content()).isEqualTo(content);
                    assertThat(res.recipientId()).isEqualTo(recipientId);
                    assertThat(res.isRead()).isFalse();
                });


        assertThat(meterRegistry.timer("messenger.message.send.latency", Tags.of("chatId", chatId.toString())).count()).isEqualTo(1);
        assertThat(meterRegistry.timer("messenger.message.send.latency", Tags.of("chatId", chatId.toString())).totalTime(TimeUnit.MILLISECONDS)).isGreaterThan(0.0);
        assertThat(meterRegistry.counter("message.message.sent.throughput").count()).isEqualTo(1.0);

        verify(userRepository).findById(senderId);
        verify(chatRepository).findById(chatId);
        verify(messageRepository).save(any(Message.class));
        verify(kafkaProducerService).sendMessage(eq("technical-metrics"), eq(senderId.toString()), any(TechnicalEvent.class));
        verifyNoMoreInteractions(messageRepository, userRepository, chatRepository, kafkaProducerService);
    }


    @Test
    @DisplayName("Test message creation: throws EntityNotFoundException when recipient not in chat")
    void createMessage_recipientNotInChat() {
        Long senderId = 1L;
        Long chatId = 100L;
        String content = "Hello";

        User sender = new User();
        sender.setId(senderId);

        Set<User> participants = new HashSet<>();
        participants.add(sender);

        Chat chat = new Chat();
        chat.setParticipants(participants);
        chat.setId(chatId);

        MessageCreateRequestDTO dto = new MessageCreateRequestDTO(content, chatId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));


        assertThatThrownBy(() -> messageService.createMessage(dto, sender))
                .isInstanceOf(EntityNotFoundException.class)
                        .hasMessageContaining("Recipient not found");

        verify(userRepository).findById(senderId);
        verify(chatRepository).findById(chatId);
    }

    @Test
    @DisplayName("Test message creation: throws IllegalArgumentException when sender not in chat")
    void createMessage_senderNotInChat() {
        Long senderId = 1L;
        Long chatId = 100L;
        String content = "Hello";

        User sender = new User();
        sender.setId(senderId);

        Set<User> participants = new HashSet<>();
        Chat chat = new Chat();
        chat.setParticipants(participants);
        chat.setId(chatId);
        MessageCreateRequestDTO dto = new MessageCreateRequestDTO(content, chatId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> messageService.createMessage(dto, sender))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not contain user with id " + senderId);

        verify(userRepository).findById(senderId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository, messageRepository);
    }

    @Test
    @DisplayName("Test successful read messages")
    void processReadReceipt() {
        Long readerId = 1L;
        Long recipientId = 3L;
        Long chatId = 2L;

        User reader = new User();
        reader.setId(readerId);

        User recipient = new User();
        recipient.setId(recipientId);

        Set<User> participants = new HashSet<>();
        participants.add(recipient);
        participants.add(reader);

        Chat chat = new Chat();
        chat.setParticipants(participants);
        chat.setId(chatId);

        when(userRepository.findById(readerId)).thenReturn(Optional.of(reader));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        Message m1 = new Message();
        m1.setId(1L);
        m1.setSendingTime(Instant.now());
        m1.setRead(false);
        m1.setSender(recipient);
        m1.setContent("Hello");

        Message m2 = new Message();
        m2.setId(4L);
        m2.setSendingTime(Instant.now());
        m2.setRead(false);
        m2.setSender(recipient);
        m2.setContent("Hello");

        List<Long> messageIdsList = List.of(m1.getId(),m2.getId());
        List<Message> messageList = List.of(m1,m2);


        ReadReceiptRequestDTO dto = new ReadReceiptRequestDTO(chatId, messageIdsList);

        when(messageRepository.findAllById(dto.messageIds())).thenReturn(messageList);

        ReadReceiptResponseDTO result = messageService.processReadReceipt(dto, reader);

        assertThat(result)
                .isNotNull()
                .satisfies(res ->{
                    assertThat(res.chatId()).isEqualTo(2L);
                    assertThat(res.messageIds()).isEqualTo(messageIdsList);
                    assertThat(res.readerId()).isEqualTo(1L);
                    assertThat(res.recipientId()).isEqualTo(3L);
                });
        assertThat(m1.isRead()).isEqualTo(true);
        assertThat(m2.isRead()).isEqualTo(true);

        verify(userRepository).findById(readerId);
        verify(messageRepository).findAllById(messageIdsList);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository,messageRepository,chatRepository);
    }
    @Test
    @DisplayName("Test read process: throws EntityNotFoundException when chat not found")
    void processReadReceipt_chatNotFound() {
        Long userId = 1L;
        Long chatId = 3L;

        User reader = new User();
        reader.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reader));
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
        Message m1 = new Message();
        m1.setId(1L);
        m1.setSendingTime(Instant.now());
        m1.setRead(false);
        m1.setContent("Hello");

        Message m2 = new Message();
        m2.setId(4L);
        m2.setSendingTime(Instant.now());
        m2.setRead(false);
        m2.setContent("Hello");

        List<Long> messageIdsList = List.of(m1.getId(),m2.getId());

        ReadReceiptRequestDTO dto = new ReadReceiptRequestDTO(chatId, messageIdsList);
        assertThatThrownBy(() ->  messageService.processReadReceipt(dto, reader))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Chat not found with id " + chatId);

        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }

    @Test
    @DisplayName("Test read process: throws IllegalArgumentException when user not in chat")
    void processReadReceipt_userNotInChat() {
        Long userId = 1L;
        Long chatId = 3L;

        User reader = new User();
        reader.setId(userId);

        Chat chat = new Chat();
        Set<User> participants = new HashSet<>(); //chat is empty
        chat.setParticipants(participants);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reader));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        Message m1 = new Message();
        m1.setId(1L);
        m1.setSendingTime(Instant.now());
        m1.setRead(false);
        m1.setContent("Hello");

        Message m2 = new Message();
        m2.setId(4L);
        m2.setSendingTime(Instant.now());
        m2.setRead(false);
        m2.setContent("Hello");

        List<Long> messageIdsList = List.of(m1.getId(),m2.getId());

        ReadReceiptRequestDTO dto = new ReadReceiptRequestDTO(chatId, messageIdsList);
        assertThatThrownBy(() ->  messageService.processReadReceipt(dto, reader))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not contain user with id " + userId);

        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }

    @Test
    @DisplayName("Test successful typing process")
    void processTyping_success() {
        Long userId = 1L;
        String username = "username";
        Long recipientId = 2L;
        Long chatId = 3L;
        boolean isTyping = true;

        User typingUser = new User();
        typingUser.setId(userId);
        typingUser.setUsername(username);

        User recipient = new User();
        recipient.setId(recipientId);

        Chat chat = new Chat();
        chat.setId(chatId);

        Set<User> participants = new HashSet<>();
        participants.add(typingUser);
        participants.add(recipient);
        chat.setParticipants(participants);

        when(userRepository.findById(userId)).thenReturn(Optional.of(typingUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        TypingDTO dto = new TypingDTO(chatId, null, null, null, isTyping);

        TypingDTO result = messageService.processTyping(dto, typingUser);

        assertThat(result)
                .isNotNull()
                .satisfies(res -> {
                   assertThat(res.chatId()).isEqualTo(chatId);
                   assertThat(res.isTyping()).isEqualTo(isTyping);
                   assertThat(res.username()).isEqualTo(username);
                   assertThat(res.recipientId()).isEqualTo(recipientId);
                   assertThat(res.userId()).isEqualTo(userId);
                });
        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }

    @Test
    @DisplayName("Test typing processing: throws IllegalArgumentException when user not in chat")
    void processTyping_userNotInChat() {
        Long userId = 1L;
        String username = "username";
        Long chatId = 3L;
        boolean isTyping = true;

        User typingUser = new User();
        typingUser.setId(userId);
        typingUser.setUsername(username);

        Chat chat = new Chat();
        chat.setId(chatId);

        Set<User> participants = new HashSet<>();
        chat.setParticipants(participants);

        when(userRepository.findById(userId)).thenReturn(Optional.of(typingUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        TypingDTO dto = new TypingDTO(chatId, null, null, null, isTyping);

        assertThatThrownBy(() -> messageService.processTyping(dto, typingUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not contain user with id " + userId);

        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }

    @Test
    @DisplayName("Test typing processing: throws EntityNotFoundException when chat not found")
    void processTyping_chatNotFound() {
        Long userId = 1L;
        Long chatId = 3L;
        boolean isTyping = true;

        User typingUser = new User();
        typingUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(typingUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        TypingDTO dto = new TypingDTO(chatId, null, null, null, isTyping);

        assertThatThrownBy(() -> messageService.processTyping(dto, typingUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Chat not found with id " + chatId);

        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }
    @Test
    @DisplayName("Test typing processing: throws EntityNotFoundException when no recipient in chat")
    void processTyping_noRecipient() {
        Long userId = 1L;
        Long chatId = 3L;
        boolean isTyping = true;

        User typingUser = new User();
        typingUser.setId(userId);

        Set<User> participants = new HashSet<>();
        participants.add(typingUser);

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setParticipants(participants);

        when(userRepository.findById(userId)).thenReturn(Optional.of(typingUser));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        TypingDTO dto = new TypingDTO(chatId, null, null, null, isTyping);

        assertThatThrownBy(() -> messageService.processTyping(dto, typingUser))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Recipient not found");

        verify(userRepository).findById(userId);
        verify(chatRepository).findById(chatId);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }
}