package com.qtra.scanner.controller;

import com.qtra.scanner.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaProducerService kafkaProducerService;

    public KafkaController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String topic, @RequestParam String key, @RequestParam String message) {
        kafkaProducerService.sendMessage(topic, key, message);
        return "Message sent to Kafka topic: " + topic;
    }
}
