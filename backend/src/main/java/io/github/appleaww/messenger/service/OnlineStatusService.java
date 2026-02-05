package io.github.appleaww.messenger.service;

import io.github.appleaww.messenger.kafka.KafkaProducerService;
import io.github.appleaww.messenger.kafka.metrics.event.BusinessEvent;
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
    private final KafkaProducerService kafkaProducerService;

    private final Map<String, LocalDateTime> sessionStartTimes = new ConcurrentHashMap<>();

    @Transactional
    public void userConnected(Long userId){
        onlineUsers.add(userId);

        userRepository.findById(userId).ifPresent(user ->{
            user.setLastSeen(Instant.now());
            user.setIsOnline(true);
            userRepository.save(user);
        });
        broadcastStatus(userId, true);
        log.debug("User with id {} connected. Active sessions: {}", userId, simpUserRegistry.getUserCount());

        BusinessEvent activeEvent = new BusinessEvent(
                "user_active",
                userId.toString(),
                null,
                LocalDateTime.now()
        );

        kafkaProducerService.sendMessage("business-metrics", activeEvent.userId(), activeEvent);
        sessionStartTimes.put(userId.toString(), LocalDateTime.now());
    }

    @Transactional
    public void userDisconnected(Long userId){
        int sessionCount = getSessionCount(userId);
        if(sessionCount == 0){
            onlineUsers.remove(userId);

            userRepository.findById(userId).ifPresent(user -> {
                user.setLastSeen(Instant.now());
                user.setIsOnline(false);
                userRepository.save(user);
            });

            broadcastStatus(userId, false);
            log.debug("User with id {} is now offline", userId);

            LocalDateTime startTime = sessionStartTimes.remove(userId.toString());
            if(startTime != null){
                long durationMs = Duration.between(startTime,LocalDateTime.now()).toMillis();
                BusinessEvent sessionEvent = new BusinessEvent(
                        "session_end",
                        userId.toString(),
                        durationMs,
                        LocalDateTime.now()
                );
                kafkaProducerService.sendMessage("business-metrics", sessionEvent.userId(), sessionEvent);
            }
        } else{
            log.debug("User with id {} disconnected one session, {} remaining", userId, sessionCount);
        }
    }

    private int getSessionCount(Long userId){
        var user = simpUserRegistry.getUser(userId.toString());
        return user!=null ? user.getSessions().size() : 0;
    }

    public boolean isUserOnline(Long userId){
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsers(){
        return Set.copyOf(onlineUsers);
    }

    public OnlineStatusDTO getStatus(Long userId){
        boolean isOnline = isUserOnline(userId);
        Instant lastSeen = isOnline ? Instant.now() : userRepository.findById(userId).map(User::getLastSeen).orElse(null);
        return new OnlineStatusDTO(userId, isOnline, lastSeen);
    }

    private void broadcastStatus(Long userId, boolean isOnline){
        Instant lastSeen = isOnline ? Instant.now() : userRepository.findById(userId).map(User::getLastSeen).orElse(Instant.now());

        OnlineStatusDTO status = new OnlineStatusDTO(userId, isOnline, lastSeen);

        simpMessagingTemplate.convertAndSend("/topic/online-status", status);
    }

    public void notifyUserAboutStatus(Long recipientId, Long targetUserId) {
        OnlineStatusDTO status = getStatus(targetUserId);
        simpMessagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/online-status",
                status
        );
    }

}
