package com.example.iot_backend.controller;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.service.LedEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/led-events")
@CrossOrigin(origins = "*")
public class LedEventController {

    private final LedEventService ledEventService;

    public LedEventController(LedEventService ledEventService) {
        this.ledEventService = ledEventService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<LedEvent>> getRecentEvents() {
        List<LedEvent> events = ledEventService.getRecentEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/device/{mac}")
    public ResponseEntity<List<LedEvent>> getEventsByDevice(@PathVariable String mac) {
        List<LedEvent> events = ledEventService.getEventsByDevice(mac);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/type/{actionType}")
    public ResponseEntity<List<LedEvent>> getEventsByType(@PathVariable String actionType) {
        List<LedEvent> events = ledEventService.getEventsByType(actionType);
        return ResponseEntity.ok(events);
    }
}
