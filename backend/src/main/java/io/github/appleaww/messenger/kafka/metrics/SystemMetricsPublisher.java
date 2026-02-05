package io.github.appleaww.messenger.kafka.metrics;

import io.github.appleaww.messenger.kafka.KafkaProducerService;
import io.github.appleaww.messenger.kafka.metrics.event.TechnicalEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemMetricsPublisher {
    private final KafkaProducerService kafkaProducerService;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 30000)
    public void publishSystemMetrics(){
        Double cpuUsage = Objects.requireNonNull(meterRegistry.find("process.cpu.usage").gauge()).value();
        Double memoryUsed = Objects.requireNonNull(meterRegistry.find("jvm.memory.used")
                        .tags("area", "heap")
                        .gauge())
                .value();

        TechnicalEvent event = new TechnicalEvent(
                "system_metrics",
                null,
                null,
                null,
                cpuUsage,
                memoryUsed,
                LocalDateTime.now()
        );
        kafkaProducerService.sendMessage("technical-metrics", "system", event);

        log.debug("System metrics sent: CPU={}, Memory={}", cpuUsage, memoryUsed);
    }
}
