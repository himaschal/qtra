package com.qtra.scanner.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaProducerService {

    private KafkaProducer<String, String> producer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @PostConstruct
    public void init() {
        // Initialize the Kafka producer
        producer = new KafkaProducer<>(createProducerConfig());
    }

    private Properties createProducerConfig() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  // Ensure correct key serialization
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

    public void sendMessage(String topic, String key, String value) {
        System.out.println("Kafka producer sendMessage");
        producer.send(new ProducerRecord<>(topic, key, value), (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent to topic " + topic + " at offset " + metadata.offset());
            } else {
                exception.printStackTrace();
            }
        });
        producer.flush(); // Ensure all messages are sent before proceeding
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            producer.close();
        }
    }
}
