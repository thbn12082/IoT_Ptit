package com.example.iot_backend.controller;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.service.LedEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

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

}
