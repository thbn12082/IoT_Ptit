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

    @GetMapping("/led/{ledNumber}")
    public ResponseEntity<List<LedEvent>> getEventsByLed(@PathVariable int ledNumber) {
        List<LedEvent> events = ledEventService.getEventsByLed(ledNumber);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEventStats() {
        Map<String, Object> stats = new HashMap<>();
        List<LedEvent> recentEvents = ledEventService.getRecentEvents();

        // Count events by LED
        Map<Integer, Long> eventsByLed = new HashMap<>();
        for (Integer ledNum : List.of(1, 2, 3, 4)) {
            long count = recentEvents.stream()
                    .filter(e -> e.getLedNumber() == ledNum)
                    .count();
            eventsByLed.put(ledNum, count);
        }

        // Get current LED states
        Map<Integer, Boolean> ledStates = new HashMap<>();
        for (Integer ledNum : List.of(1, 2, 3, 4)) {
            boolean state = recentEvents.stream()
                    .filter(e -> e.getLedNumber() == ledNum)
                    .findFirst()
                    .map(LedEvent::getStateOn)
                    .orElse(false);
            ledStates.put(ledNum, state);
        }

        stats.put("total_events", recentEvents.size());
        stats.put("events_by_led", eventsByLed);
        stats.put("current_states", ledStates);
        stats.put("last_updated", LocalDateTime.now().toString());

        return ResponseEntity.ok(stats);
    }
}
