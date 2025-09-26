package com.example.iot_backend.controller;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.service.LedEventService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/led-events")
@CrossOrigin(origins = "*")
public class LedEventController {

    private final LedEventService ledEventService;

    public LedEventController(LedEventService ledEventService) {
        this.ledEventService = ledEventService;
    }

    // EXISTING method (keep for backward compatibility)
    @GetMapping("/recent")
    public ResponseEntity<List<LedEvent>> getRecentEvents() {
        List<LedEvent> events = ledEventService.getRecentEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalRecords = ledEventService.getTotalRecords();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", totalRecords);
        stats.put("status", "OK");

        return ResponseEntity.ok(stats);
    }

    // NEW: Advanced paginated search for action history
    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getLedEventsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String deviceFilter,
            @RequestParam(required = false) String timeFilter) {

        try {
            Page<LedEvent> pageData = ledEventService.getLedEventsPaginated(page, size, search, deviceFilter, timeFilter);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageData.getContent());
            response.put("currentPage", pageData.getNumber());
            response.put("totalPages", pageData.getTotalPages());
            response.put("totalElements", pageData.getTotalElements());
            response.put("size", pageData.getSize());
            response.put("first", pageData.isFirst());
            response.put("last", pageData.isLast());
            response.put("search", search);
            response.put("deviceFilter", deviceFilter);
            response.put("timeFilter", timeFilter);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Specific device endpoints
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Map<String, Object>> getEventsByDevice(
            @PathVariable int deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<LedEvent> pageData = ledEventService.getLedEventsPaginated(page, size, "", String.valueOf(deviceId), null);

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageData.getContent());
        response.put("currentPage", pageData.getNumber());
        response.put("totalPages", pageData.getTotalPages());
        response.put("totalElements", pageData.getTotalElements());
        response.put("deviceId", deviceId);
        response.put("deviceName", LedEventService.getDeviceName(deviceId));

        return ResponseEntity.ok(response);
    }

}
