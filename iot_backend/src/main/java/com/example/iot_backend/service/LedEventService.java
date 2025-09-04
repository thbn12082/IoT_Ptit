package com.example.iot_backend.service;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LedEventService {

    private final LedEventRepository repository;

    public LedEventService(LedEventRepository repository) {
        this.repository = repository;
    }

    // ✅ Sử dụng no-args constructor + setters
    @Transactional
    public LedEvent save(String deviceMac, int ledNumber, String actionType,
                         boolean stateOn, String topic, String payload, String source) {

        LedEvent event = new LedEvent();  // ✅ No-args constructor
        event.setDeviceMac(deviceMac);
        event.setLedNumber(ledNumber);
        event.setActionType(actionType);
        event.setStateOn(stateOn);
        event.setTopic(topic);
        event.setPayload(payload);
        event.setSource(source);
        event.setCreatedAt(LocalDateTime.now());

        return repository.save(event);
    }

    public List<LedEvent> getRecentEvents() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<LedEvent> getEventsByDevice(String deviceMac) {
        return repository.findTop50ByDeviceMacOrderByCreatedAtDesc(deviceMac);
    }

    public List<LedEvent> getEventsByType(String actionType) {
        return repository.findByActionTypeOrderByCreatedAtDesc(actionType);
    }
}
