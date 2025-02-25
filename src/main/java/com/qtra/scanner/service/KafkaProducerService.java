package com.qtra.scanner.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.*;
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
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
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
