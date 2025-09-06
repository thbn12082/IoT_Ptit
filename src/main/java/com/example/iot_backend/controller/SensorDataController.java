package com.example.iot_backend.controller;

import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "http://localhost:3000")
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    // Lấy tất cả dữ liệu sensor
    @GetMapping
    public ResponseEntity<List<SensorData>> getAllSensorData() {
        List<SensorData> data = sensorDataService.getAllSensorData();
        return ResponseEntity.ok(data);
    }

    // Lấy dữ liệu mới nhất
    @GetMapping("/latest")
    public ResponseEntity<List<SensorData>> getLatestSensorData() {
        List<SensorData> data = sensorDataService.getLatestSensorData();
        return ResponseEntity.ok(data);
    }

    // Lấy dữ liệu 24h gần nhất
    @GetMapping("/recent")
    public ResponseEntity<List<SensorData>> getRecentSensorData() {
        List<SensorData> data = sensorDataService.getRecentSensorData();
        return ResponseEntity.ok(data);
    }

    // Lấy dữ liệu trong khoảng thời gian
    @GetMapping("/range")
    public ResponseEntity<List<SensorData>> getSensorDataByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<SensorData> data = sensorDataService.getSensorDataByDateRange(start, end);
        return ResponseEntity.ok(data);
    }

    // Lấy dữ liệu theo ID
    @GetMapping("/{id}")
    public ResponseEntity<SensorData> getSensorDataById(@PathVariable Long id) {
        Optional<SensorData> data = sensorDataService.getSensorDataById(id);

        if (data.isPresent()) {
            return ResponseEntity.ok(data.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Thống kê
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalRecords = sensorDataService.getTotalRecords();

        return ResponseEntity.ok(new Object() {
            public final long total_records = totalRecords;
            public final String status = "OK";
        });
    }
}
