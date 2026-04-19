package io.github.appleaww.messenger.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public void userRegistered(){
        meterRegistry.counter("messenger.users.registered").increment();
    }
    public void activityUserLogged(){
        meterRegistry.counter("messenger.user.activity","action_type", "login").increment();
    }
    public void chatCreated(){
        meterRegistry.counter("messenger.chats.created").increment();
    }
    public void activitySessionStarted(String userId){
        meterRegistry.counter("messenger.user.activity","userId", userId, "action_type", "session_started").increment();
    }
    public void messageSent(){
        meterRegistry.counter("messenger.messages.sent").increment();
    }
    public void subscriptionStarted(String tier){
        meterRegistry.counter("messenger.subscriptions.started", "tier", tier).increment();
    }
    public void sessionDuration(LocalDateTime startTime){
        if (startTime != null) {
            long durationMs = Duration.between(startTime, LocalDateTime.now()).toMillis();

            meterRegistry.summary("messenger.sessions.duration").record(durationMs);
        }
    }
    public MessageSendTimerContext startMessageSendLatency() {
        Timer timer = meterRegistry.timer("messenger.messages.send.latency");

        Timer.Sample sample = Timer.start(meterRegistry);
        return new MessageSendTimerContext(sample, timer);
    }
    public record MessageSendTimerContext(Timer.Sample sample, Timer timer) {
        public void stop() {
            sample.stop(timer);
        }
    }
}
