package io.github.appleaww.messenger.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, String key, Object message){
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
        future.orTimeout(10, TimeUnit.SECONDS);
        future.whenComplete((result, throwable) -> {
            if(throwable == null){
                log.debug("Message sent to topic {}, key {}, offset {}", topic, key, result.getRecordMetadata().offset());
            }else{
                log.error("Failed to send to topic {}, key {}: {}", topic, key, throwable.getMessage());
            }
        });
    }
}
