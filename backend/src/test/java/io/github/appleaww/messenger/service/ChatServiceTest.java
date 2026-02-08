package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.mapper.ChatMapper;
import io.github.appleaww.messenger.model.dto.ChatListItemDTO;
import io.github.appleaww.messenger.model.dto.ParticipantDTO;
import io.github.appleaww.messenger.model.dto.request.ChatCreateRequestDTO;
import io.github.appleaww.messenger.model.dto.response.ChatCreateResponseDTO;
import io.github.appleaww.messenger.model.entity.Chat;
import io.github.appleaww.messenger.model.entity.Message;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.ChatRepository;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ChatMapper chatMapper;
    @Spy @InjectMocks private ChatService chatService;


    @Test
    @DisplayName("Test successful chat creation")
    void createChat() {
        Long initiatorId = 1L;
        String initiatorName = "Artem";
        String initiatorUsername = "qweqweq";
        Long companionId = 2L;
        String companionName = "Alex";
        String companionUsername = "qwert";
        Long chatId = 3L;

        User companion  = new User();
        companion.setName(companionName);
        companion.setId(companionId);
        companion.setUsername(companionUsername);

        User initiator = new User();
        initiator.setId(initiatorId);
        initiator.setName(initiatorName);
        initiator.setUsername(initiatorUsername);

        when(userRepository.findByUsername(companionUsername)).thenReturn(Optional.of(companion));
        when(chatRepository.findExistingChatBetweenUserIds(eq(initiatorId),eq(companionId))).thenReturn(Optional.empty());
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat chat = invocation.getArgument(0);
            chat.setId(chatId);
            return chat;
        });


        ChatCreateResponseDTO expectedDto = new ChatCreateResponseDTO(chatId, "Send the first message!", companionName,
                List.of(
                        new ParticipantDTO(companionId, companionName),
                        new ParticipantDTO(initiatorId, initiatorName)
                )
        );
        when(chatMapper.toDTO(any(Chat.class), eq(companion))).thenReturn(expectedDto);

        ChatCreateRequestDTO dto = new ChatCreateRequestDTO(companionUsername);

        ChatCreateResponseDTO result = chatService.createChat(dto, initiator);

        assertThat(result)
                .isNotNull()
                .satisfies(res -> {
                    assertThat(res.id()).isEqualTo(chatId);
                    assertThat(res.lastMessage()).isEqualTo("Send the first message!");
                    assertThat(res.companionName()).isEqualTo(companionName);
                    assertThat(res.participantDTOList()).containsExactlyInAnyOrder(
                            new ParticipantDTO(companionId, companionName),
                            new ParticipantDTO(initiatorId, initiatorName)
                    );
                });
        verify(userRepository).findByUsername(companionUsername);
        verify(chatRepository).findExistingChatBetweenUserIds(initiatorId,companionId);
        verify(chatRepository).save(any(Chat.class));
        verifyNoMoreInteractions(userRepository,chatRepository);

    }
    @Test
    @DisplayName("Test chat creation: throws EntityNotFoundException when companion username does not exist")
    void createChat_CompanionNotFound() {
        Long initiatorId = 1L;
        String nonExistentUsername = "12345";

        User initiator = new User();
        initiator.setId(initiatorId);

        ChatCreateRequestDTO dto = new ChatCreateRequestDTO(nonExistentUsername);

        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.createChat(dto, initiator))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found with username " + nonExistentUsername);

        verify(userRepository).findByUsername(nonExistentUsername);
        verifyNoInteractions(chatRepository, chatMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Test chat creation: throw RuntimeException when initiator tries to chat with himself")
    void createChat_SelfChat() {
        Long sameUserId = 1L;
        String sameUsername = "Artem";

        User initiator = new User();
        initiator.setId(sameUserId);

        User selfCompanion = new User();
        selfCompanion.setId(sameUserId);
        selfCompanion.setUsername(sameUsername);

        ChatCreateRequestDTO dto = new ChatCreateRequestDTO(sameUsername);

        when(userRepository.findByUsername(sameUsername)).thenReturn(Optional.of(selfCompanion));

        assertThatThrownBy(() -> chatService.createChat(dto, initiator))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot create chat with yourself");

        verify(userRepository).findByUsername(sameUsername);
        verifyNoInteractions(chatRepository, chatMapper);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Test chat creation: throw RuntimeException when chat between users already exists")
    void createChat_ExistingChat() {
        Long initiatorId = 1L;
        String initiatorName = "Artem";
        Long companionId = 2L;
        String companionName = "Alex";
        String companionUsername = "qwert";

        User companion = new User();
        companion.setId(companionId);
        companion.setName(companionName);
        companion.setUsername(companionUsername);

        User initiator = new User();
        initiator.setId(initiatorId);
        initiator.setName(initiatorName);

        ChatCreateRequestDTO dto = new ChatCreateRequestDTO(companionUsername);

        when(userRepository.findByUsername(companionUsername)).thenReturn(Optional.of(companion));
        Chat existingChat = new Chat();
        when(chatRepository.findExistingChatBetweenUserIds(eq(initiatorId), eq(companionId)))
                .thenReturn(Optional.of(existingChat));

        assertThatThrownBy(() -> chatService.createChat(dto, initiator))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chat already exists");

        verify(userRepository).findByUsername(companionUsername);
        verify(chatRepository).findExistingChatBetweenUserIds(eq(initiatorId), eq(companionId));
        verifyNoInteractions(chatMapper);
        verifyNoMoreInteractions(userRepository, chatRepository);
    }



    @Test
    @DisplayName("Test successful close certain chat ")
    void closeCertainChat() {
        Long chatId = 1L;
        Long userId = 2L;
        String userName = "Artem";

        User user = new User();
        user.setId(userId);
        user.setName(userName);

        doNothing().when(chatService).saveLastMessage(eq(chatId));

        List<ChatListItemDTO> expectedChats = List.of(
                new ChatListItemDTO(1L, 3L, "Companion1", "Last msg 1", null, 0L),
                new ChatListItemDTO(2L, 4L, "Companion2", "Last msg 2", null, 1L)
        );
        doReturn(expectedChats).when(chatService).getAllUserChatsWithDetails(eq(user));

        List<ChatListItemDTO> result = chatService.closeCertainChat(chatId, user);

        assertThat(result)
                .isNotNull()
                .isEqualTo(expectedChats);

        verify(chatService, times(1)).saveLastMessage(eq(chatId));
        verify(chatService, times(1)).getAllUserChatsWithDetails(eq(user));
        verifyNoInteractions(userRepository, chatRepository, chatMapper);
    }

    @Test
    void saveLastMessage() {
    }

    @Test
    @DisplayName("Test successful getAllUserChatsWithDetails")
    void getAllUserChatsWithDetails() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        User refreshedUser = new User();
        refreshedUser.setId(userId);

        Chat chat1 = new Chat();
        chat1.setId(10L);
        chat1.setLastMessage("m1");

        User companion1 = new User();
        companion1.setId(2L);
        companion1.setName("Artem");

        Message lastMsg1 = new Message();
        lastMsg1.setContent("m2");
        lastMsg1.setSendingTime(Instant.parse("2100-01-01T10:00:00Z"));
        lastMsg1.setRead(false);
        lastMsg1.setSender(companion1);

        Message readMsg1 = new Message();
        readMsg1.setContent("m3");
        readMsg1.setSendingTime(Instant.parse("2100-01-01T09:00:00Z"));
        readMsg1.setRead(true);
        readMsg1.setSender(user);

        chat1.setParticipants(Set.of(user, companion1));
        chat1.setMessages(List.of(readMsg1, lastMsg1));

        Chat chat2 = new Chat();
        chat2.setId(20L);
        chat2.setLastMessage("Send the first message!");

        User companion2 = new User();
        companion2.setId(3L);
        companion2.setName("Andrey");

        Message readMsg2 = new Message();
        readMsg2.setContent(null);
        readMsg2.setSendingTime(Instant.parse("2100-01-02T10:00:00Z"));
        readMsg2.setRead(true);
        readMsg2.setSender(companion2);

        chat2.setParticipants(Set.of(user, companion2));
        chat2.setMessages(List.of(readMsg2));

        refreshedUser.setChats(Set.of(chat2, chat1));

        when(userRepository.findWithChatsAndMessagesById(userId)).thenReturn(Optional.of(refreshedUser));

        List<ChatListItemDTO> result = chatService.getAllUserChatsWithDetails(user);

        ChatListItemDTO expectedChat2 = new ChatListItemDTO(
                20L, 3L, "Andrey", "Send the first message!", Instant.parse("2100-01-02T10:00:00Z"), 0L);
        ChatListItemDTO expectedChat1 = new ChatListItemDTO(
                10L, 2L, "Artem", "m2", Instant.parse("2100-01-01T10:00:00Z"), 1L);

        assertThat(result)
                .hasSize(2)
                .containsExactly(expectedChat2, expectedChat1)
                .isSortedAccordingTo(Comparator.comparing(ChatListItemDTO::lastMessageSendingTime).reversed());

        verify(userRepository, times(1)).findWithChatsAndMessagesById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteChat() {
    }

    @Test
    void openChat() {
    }
}