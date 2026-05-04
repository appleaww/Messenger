package io.github.appleaww.messenger.kafka;


import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.module.afterburner.AfterburnerModule;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class KafkaProducerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(new AfterburnerModule())
                .build();

        JacksonJsonSerializer<Object> jsonSerializer = new JacksonJsonSerializer<>(jsonMapper);

        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setValueSerializer(jsonSerializer);

        return factory;
    }

    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String, Object> configs = new HashMap<>();
        configs.putAll(kafkaProperties.buildAdminProperties());
        return new KafkaAdmin(configs);
    }

    private Map<String, String> defaultMetricTopicConfigs() {
        Map<String, String> configs = new HashMap<>();
        configs.put(TopicConfig.RETENTION_MS_CONFIG, "604800000");
        configs.put(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(TopicConfig.SEGMENT_BYTES_CONFIG, "1073741824");
        return configs;
    }

    @Bean
    public NewTopic businessMetricsTopic() {
        return new NewTopic("business-metrics", 6, (short) 2)
                .configs(defaultMetricTopicConfigs());
    }

    @Bean
    public NewTopic technicalMetricsTopic() {
        return new NewTopic("technical-metrics", 6, (short) 2)
                .configs(defaultMetricTopicConfigs());
    }

    @Bean
    public NewTopic userActivityMetricsTopic(){
        return new NewTopic("user-activity-events",3, (short) 2)
                .configs(defaultMetricTopicConfigs());
    }
}
