package com.example.iot_backend.controller;

import com.example.iot_backend.dto.DeviceInfo;
import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceInfo>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/{mac}")
    public ResponseEntity<DeviceInfo> getDeviceByMac(@PathVariable String mac) {
        DeviceInfo device = deviceService.getDeviceInfo(mac);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @GetMapping("/online")
    public ResponseEntity<List<DeviceInfo>> getOnlineDevices() {
        return ResponseEntity.ok(deviceService.getOnlineDevices());
    }


    @GetMapping("/stats")
    public ResponseEntity<?> getDeviceStats() {
        long onlineCount = deviceService.countOnlineDevices();
        long totalCount = deviceService.getAllDevices().size();

        return ResponseEntity.ok(new Object() {
            public final long online_devices = onlineCount;
            public final long total_devices = totalCount;
            public final double online_percentage = totalCount > 0 ? (double) onlineCount / totalCount * 100 : 0;
            public final String last_updated = LocalDateTime.now().toString();
        });
    }

    // ✅ THÊM: API lấy events gần đây
    @GetMapping("/recent-events")
    public ResponseEntity<List<LedEvent>> getRecentEvents(@RequestParam(defaultValue = "60") int minutes) {
        List<LedEvent> events = deviceService.getRecentEvents(minutes);
        return ResponseEntity.ok(events);
    }


    // ✅ Trả về success message thay vì LedEvent
    @PostMapping("/{mac}/led/{ledNumber}")
    public ResponseEntity<Map<String, Object>> controlLed(
            @PathVariable String mac,
            @PathVariable int ledNumber,
            @RequestBody boolean state) {

        try {
            deviceService.controlLed(mac, ledNumber, state);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("LED%d %s command sent", ledNumber, state ? "ON" : "OFF"));
            response.put("device_mac", mac);
            response.put("led_number", ledNumber);
            response.put("state", state);
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ... Các endpoints khác giữ nguyên



}
