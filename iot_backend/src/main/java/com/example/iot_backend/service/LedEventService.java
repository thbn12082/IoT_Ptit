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

    @Transactional
    public LedEvent save(int ledNumber, boolean stateOn) {
        LedEvent event = new LedEvent();
        event.setLedNumber(ledNumber);
        event.setStateOn(stateOn);
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }

    public List<LedEvent> getRecentEvents() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<LedEvent> getEventsByLed(int ledNumber) {
        return repository.findByLedNumberOrderByCreatedAtDesc(ledNumber);
    }

}
