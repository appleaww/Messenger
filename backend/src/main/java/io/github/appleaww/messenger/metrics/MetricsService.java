package io.github.appleaww.messenger.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void userRegistered(String userRole){
        meterRegistry.counter("messenger.users.registered", "role", userRole).increment();
    }
    public void activityUserLogged(String userId){
        meterRegistry.counter("messenger.user.activity", "userId", userId ,"action_type", "login").increment();
    }
    public void chatCreated(String initiatorId){
        meterRegistry.counter("messenger.chats.created","userId", initiatorId).increment();
    }
    public void activityChatOpened(String userId){
        meterRegistry.counter("messenger.user.activity", "userId", userId, "action_type", "chat_opened").increment();
    }
    public void activitySessionStarted(String userId){
        meterRegistry.counter("messenger.user.activity", "userId", userId, "action_type", "session_started").increment();
    }
    public void sessionStarted(String userId){
        meterRegistry.counter("messenger.sessions.started", "userId", userId).increment();
    }
    public void messageSent(String senderId, String chatId){
        meterRegistry.counter("messenger.messages.sent", "userId", senderId, "chatId", chatId).increment();
    }
    public void subscriptionStarted(String tier, String userId){
        meterRegistry.counter("messenger.subscriptions.started", "tier", tier, "userId", userId).increment();

    }
    public void sessionDuration(LocalDateTime startTime, String userId){
        if (startTime != null) {
            long durationMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

            meterRegistry.summary("messenger.sessions.duration",
                    "userId", userId).record(durationMs);
        }
    }
    public MessageSendTimerContext startMessageSendLatency(String senderId, String chatId) {
        Timer timer = meterRegistry.timer("messenger.messages.send.latency", "userId", senderId, "chatId", chatId);

        Timer.Sample sample = Timer.start(meterRegistry);
        return new MessageSendTimerContext(sample, timer);
    }
    public record MessageSendTimerContext(Timer.Sample sample, Timer timer) {
        public void stop() {
            sample.stop(timer);
        }
    }
}
