package io.github.appleaww.messenger.metrics;

import io.github.appleaww.messenger.kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService;

    public void userRegistered(){
        meterRegistry.counter("messenger.users.registered").increment();
    }
    public void userLogged(){
        meterRegistry.counter("messenger.users.login").increment();
    }
    public void chatCreated(){
        meterRegistry.counter("messenger.chats.created").increment();
    }
    public void recordUserActivity(String userId, String actionType){
        kafkaProducerService.sendUserActivity(userId, actionType);
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
