package com.example.iot_backend.service;

import com.example.iot_backend.gateway.MqttGateway;
import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final LedEventRepository ledEventRepository;
    private final MqttGateway mqttGateway;

    public DeviceService(LedEventRepository ledEventRepository, MqttGateway mqttGateway) {
        this.ledEventRepository = ledEventRepository;
        this.mqttGateway = mqttGateway;
    }

    // Control LED
    @Transactional
    public LedEvent controlLed(int ledNumber, boolean state) {
        logger.info("🎯 Controlling LED{}: {}", ledNumber, state ? "ON" : "OFF");

        // Send MQTT command
        mqttGateway.sendToMqtt("home/lamps/" + ledNumber, state ? "1" : "0");
        logger.info("📤 MQTT command sent: home/lamps/{} -> {}", ledNumber, state ? "1" : "0");

        // Create a new LED event
        LedEvent event = new LedEvent();
        event.setLedNumber(ledNumber);
        event.setStateOn(state);
        return ledEventRepository.save(event);
    }

    // Get LED state
    public Optional<Boolean> getLedState(int ledNumber) {
        List<LedEvent> events = ledEventRepository.findLatestLedState(ledNumber);
        if (events.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(events.get(0).getStateOn());
    }

    // Get recent LED events
    public List<LedEvent> getRecentEvents() {
        return ledEventRepository.findTop50ByOrderByCreatedAtDesc();
    }

    // Get events for a specific LED
    public List<LedEvent> getLedEvents(int ledNumber) {
        return ledEventRepository.findByLedNumberOrderByCreatedAtDesc(ledNumber);
    }
}
