package com.example.iot_backend.service;

import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SensorDataService {

    @Autowired
    private SensorDataRepository sensorDataRepository;

    // Lưu dữ liệu sensor
    public SensorData saveSensorData(SensorData sensorData) {
        return sensorDataRepository.save(sensorData);
    }

    // Lấy tất cả dữ liệu, thời gian được sắp xếp từ mới đến cũ
    public List<SensorData> getAllSensorData() {
        return sensorDataRepository.findAllByOrderByCreatedAtDesc();
    }

    // Lấy dữ liệu mới nhất (10 records)
    public List<SensorData> getLatestSensorData() {
        return sensorDataRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // Lấy dữ liệu trong khoảng thời gian
    public List<SensorData> getSensorDataByDateRange(LocalDateTime start, LocalDateTime end) {
        return sensorDataRepository.findByDateRange(start, end);
    }

    // Lấy dữ liệu 24h gần nhất
    public List<SensorData> getRecentSensorData() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return sensorDataRepository.findRecentData(since);
    }

    // Lấy dữ liệu theo ID
    public Optional<SensorData> getSensorDataById(Long id) {
        return sensorDataRepository.findById(id);
    }

    // Đếm tổng số records
    public long getTotalRecords() {
        return sensorDataRepository.count();
    }

    // Xóa dữ liệu cũ (older than X days)
    public void cleanupOldData(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        // Implement cleanup logic if needed
    }
}
