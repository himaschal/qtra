package com.qtra.scanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.utils.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;

@Service
public class KafkaProducerService {

    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    public KafkaProducerService(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String key, Object value) {
        try {
            // Ensure JSON is serialized only once
            String json = (value instanceof String) ? (String) value : objectMapper.writeValueAsString(value);

            producer.send(new ProducerRecord<>(topic, key, json), (metadata, exception) -> {
                if (exception != null) {
                    logger.error("Failed to send message to topic {}: {}", topic, exception.getMessage(), exception);
                } else {
                    logger.info("Successfully sent message to {} with key {} and json {}", topic, key, json);
                }
            });
        } catch (Exception e) {
            logger.error("Exception while sending Kafka message: ", e);
        }
    }
}
