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
    // cho FE gọi api này để lấy lịch sử đèn

}
// Luồng Hoạt Động Trong Hệ Thống
//1. Khi User Điều Khiển LED từ Dashboard
//Frontend Dashboard → WebSocket/REST API →
//MqttService (gửi lệnh tới device) →
//LedEventService.save() → Database

//2. Khi Device Feedback Trạng thái
//IoT Device → MQTT → MqttService →
//LedEventService.save() → Database →
//WebSocketService (broadcast tới dashboard)

//3. Khi Dashboard Cần Hiển Thị Lịch Sử
//Frontend → GET /api/led-events/recent →
//LedEventController → LedEventService →
//LedEventRepository → Database
