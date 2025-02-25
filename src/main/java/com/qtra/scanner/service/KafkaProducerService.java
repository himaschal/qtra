package com.qtra.scanner.service;

import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

//@Service
public class KafkaProducerService {
    private final Producer<String, String> producer;

    public KafkaProducerService(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        System.out.println("Kafka Bootstrap Servers: " + bootstrapServers);  // Debugging

        this.producer = new KafkaProducer<>(props);
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
        producer.flush();
    }

    @PreDestroy
    public void close() {
        producer.close();
        System.out.println("Kafka Producer closed.");
    }
}
