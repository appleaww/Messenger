package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.metrics.MetricsService;
import io.github.appleaww.messenger.model.dto.OnlineStatusDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.Optional;
import java.util.Set;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnlineStatusServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @Mock private SimpUserRegistry simpUserRegistry;
    @Mock private MetricsService metricsService;
    @InjectMocks private OnlineStatusService onlineStatusService;

    @Test
    @DisplayName("Test successful user connection when user exists in database")
    void userConnected_UserExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        onlineStatusService.userConnected(userId);

        assertThat(onlineStatusService.isUserOnline(userId)).isTrue();

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);

        ArgumentCaptor<OnlineStatusDTO> statusCaptor = ArgumentCaptor.forClass(OnlineStatusDTO.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/online-status"), statusCaptor.capture());
        OnlineStatusDTO sentStatus = statusCaptor.getValue();
        assertThat(sentStatus.userId()).isEqualTo(userId);
        assertThat(sentStatus.isOnline()).isTrue();
        assertThat(sentStatus.lastSeen()).isNotNull();

        verify(metricsService).recordUserActivity(eq(userId.toString()), eq("session_started"));
        verify(simpUserRegistry).getUserCount();
        verifyNoMoreInteractions(userRepository, simpMessagingTemplate, metricsService);
    }

    @Test
    @DisplayName("Test user connection when user does not exist in database")
    void userConnected_UserNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        onlineStatusService.userConnected(userId);

        assertThat(onlineStatusService.isUserOnline(userId)).isTrue();

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));

        ArgumentCaptor<OnlineStatusDTO> statusCaptor = ArgumentCaptor.forClass(OnlineStatusDTO.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/online-status"), statusCaptor.capture());
        OnlineStatusDTO sentStatus = statusCaptor.getValue();
        assertThat(sentStatus.userId()).isEqualTo(userId);
        assertThat(sentStatus.isOnline()).isTrue();
        assertThat(sentStatus.lastSeen()).isNotNull();

        verify(metricsService).recordUserActivity(eq(userId.toString()), eq("session_started"));
        verify(simpUserRegistry).getUserCount();
        verifyNoMoreInteractions(userRepository, simpMessagingTemplate, metricsService);
    }

    @Test
    @DisplayName("Test successful user disconnection when it is the last session")
    void userDisconnected_LastSession() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(simpUserRegistry.getUser(userId.toString())).thenReturn(null);

        onlineStatusService.userDisconnected(userId);

        assertThat(onlineStatusService.isUserOnline(userId)).isFalse();

        verify(simpUserRegistry).getUser(userId.toString());

        verify(userRepository, times(2)).findById(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(ReflectionTestUtils.getField(savedUser, "isOnline")).isEqualTo(false);
        assertThat(ReflectionTestUtils.getField(savedUser, "lastSeen")).isNotNull();

        ArgumentCaptor<OnlineStatusDTO> statusCaptor = ArgumentCaptor.forClass(OnlineStatusDTO.class);
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/online-status"), statusCaptor.capture());
        OnlineStatusDTO sentStatus = statusCaptor.getValue();
        assertThat(sentStatus.userId()).isEqualTo(userId);
        assertThat(sentStatus.isOnline()).isFalse();
        assertThat(sentStatus.lastSeen()).isNotNull();

        verify(metricsService).sessionDuration(isNull());
        verifyNoMoreInteractions(userRepository, simpMessagingTemplate, metricsService, simpUserRegistry);
    }


    @Test
    @DisplayName("Test isUserOnline returns false for user who never connected")
    void isUserOnline_NotOnline() {
        assertThat(onlineStatusService.isUserOnline(999L)).isFalse();
        verifyNoInteractions(userRepository, simpMessagingTemplate, simpUserRegistry, metricsService);
    }

    @Test
    @DisplayName("Test getOnlineUsers returns empty set initially")
    void getOnlineUsers_EmptyInitially() {
        Set<Long> result = onlineStatusService.getOnlineUsers();

        assertThat(result).isNotNull().isEmpty();
        verifyNoInteractions(userRepository, simpMessagingTemplate, simpUserRegistry, metricsService);
    }
}
