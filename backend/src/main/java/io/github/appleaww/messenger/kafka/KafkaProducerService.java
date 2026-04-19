package io.github.appleaww.messenger.kafka;

import io.github.appleaww.messenger.metrics.event.UserActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserActivity(String userId, String actionType){
        try{
        UserActivityEvent userActivityEvent = new UserActivityEvent(
                userId,
                actionType,
                Instant.now()
        );
        String key = userId;

        kafkaTemplate.send("user-activity-events", key, userActivityEvent)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("user.activity event(action: {}) sent successfully for user {} to topic {} ",
                                actionType,
                                userId,
                                result.getRecordMetadata().topic());

                    } else {
                        log.error("Error: send user.activity event(action: {}) for user {}",
                                actionType, userId, exception);
                    }
                });
        }catch(Exception e){
            log.error("Error: send user.activity event(action: {}) for user {}", actionType, userId, e);
    }
  }
}
