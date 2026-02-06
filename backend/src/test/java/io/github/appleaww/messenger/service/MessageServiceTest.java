package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.kafka.KafkaProducerService;
import io.github.appleaww.messenger.kafka.metrics.event.TechnicalEvent;
import io.github.appleaww.messenger.model.dto.request.MessageCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.response.MessageCreateResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.MessageRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
    void processReadReceipt() {
    }

    @Test
    void processTyping() {
    }
}