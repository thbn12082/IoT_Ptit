package com.example.iot_backend.controller;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/leds")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping("/{ledNumber}/state")
    public ResponseEntity<Boolean> getLedState(@PathVariable int ledNumber) {
        Optional<Boolean> state = deviceService.getLedState(ledNumber);
        return state.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{ledNumber}/history")
    public ResponseEntity<List<LedEvent>> getLedHistory(@PathVariable int ledNumber) {
        List<LedEvent> events = deviceService.getLedEvents(ledNumber);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDeviceStats() {
        List<LedEvent> recentEvents = deviceService.getRecentEvents();
        long activeLedsCount = recentEvents.stream()
                .filter(LedEvent::getStateOn)
                .map(LedEvent::getLedNumber)
                .distinct()
                .count();

        return ResponseEntity.ok(new Object() {
            public final long active_leds = activeLedsCount;
            public final int total_leds = 4; // Since we have 4 LEDs in our system
            public final double active_percentage = (double) activeLedsCount / 4 * 100;
            public final String last_updated = LocalDateTime.now().toString();
        });
    }

    // API to get all recent events
    @GetMapping("/recent-events")
    public ResponseEntity<List<LedEvent>> getRecentEvents() {
        List<LedEvent> events = deviceService.getRecentEvents();
        return ResponseEntity.ok(events);
    }

    // Return success message instead of LedEvent
    @PostMapping("/{ledNumber}/control")
    public ResponseEntity<Map<String, Object>> controlLedWithResponse(
            @PathVariable int ledNumber,
            @RequestParam boolean state) {

        try {
            LedEvent event = deviceService.controlLed(ledNumber, state);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("LED%d %s command sent", ledNumber, state ? "ON" : "OFF"));
            response.put("led_number", ledNumber);
            response.put("state", state);
            response.put("timestamp", event.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

}
