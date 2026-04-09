package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.metrics.MetricsService;
import io.github.appleaww.messenger.model.dto.OnlineStatusDTO;
import io.github.appleaww.messenger.model.entity.User;
import io.github.appleaww.messenger.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineStatusService {

    private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final SimpUserRegistry simpUserRegistry;
    private final UserRepository userRepository;
    private final Map<String, LocalDateTime> sessionStartTimes = new ConcurrentHashMap<>();
    private final MetricsService metricsService;

    @Transactional
    public void userConnected(Long userId) {
        onlineUsers.add(userId);

        userRepository.findById(userId).ifPresent(user -> {
            user.setLastSeen(Instant.now());
            user.setIsOnline(true);
            userRepository.save(user);
        });

        broadcastStatus(userId, true);
        log.debug("User with id {} connected. Active sessions: {}", userId, simpUserRegistry.getUserCount());

        sessionStartTimes.put(userId.toString(), LocalDateTime.now());

        metricsService.activitySessionStarted(userId.toString());
        metricsService.sessionStarted(userId.toString());
    }

    @Transactional
    public void userDisconnected(Long userId) {
        int sessionCount = getSessionCount(userId);

        if (sessionCount == 0) {
            onlineUsers.remove(userId);

            userRepository.findById(userId).ifPresent(user -> {
                user.setLastSeen(Instant.now());
                user.setIsOnline(false);
                userRepository.save(user);
            });

            broadcastStatus(userId, false);
            log.debug("User with id {} is now offline", userId);
        } else {
            log.debug("User with id {} disconnected one session, {} remaining", userId, sessionCount);
        }
        metricsService.sessionDuration(sessionStartTimes.remove(userId.toString()), userId.toString());

    }

    private int getSessionCount(Long userId) {
        var user = simpUserRegistry.getUser(userId.toString());
        return user != null ? user.getSessions().size() : 0;
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }

    private void broadcastStatus(Long userId, boolean isOnline) {
        Instant lastSeen = isOnline
                ? Instant.now()
                : userRepository.findById(userId).map(User::getLastSeen).orElse(Instant.now());

        OnlineStatusDTO status = new OnlineStatusDTO(userId, isOnline, lastSeen);
        simpMessagingTemplate.convertAndSend("/topic/online-status", status);
    }
}