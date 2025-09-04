package com.example.iot_backend.repository;

import com.example.iot_backend.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Tìm dữ liệu mới nhất của device
    @Query("SELECT s FROM SensorData s WHERE s.deviceMac = :mac ORDER BY s.createdAt DESC")
    List<SensorData> findLatestByDeviceMac(@Param("mac") String mac);

    // Tìm dữ liệu trong khoảng thời gian
    @Query("SELECT s FROM SensorData s WHERE s.createdAt BETWEEN :start AND :end ORDER BY s.createdAt DESC")
    List<SensorData> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Tìm dữ liệu mới nhất (top 10)
    @Query("SELECT s FROM SensorData s ORDER BY s.createdAt DESC LIMIT 10")
    List<SensorData> findLatest10();

    // Tìm dữ liệu theo device MAC
    List<SensorData> findByDeviceMacOrderByCreatedAtDesc(String deviceMac);

    // Đếm số record của device
    long countByDeviceMac(String deviceMac);

    // Tìm dữ liệu trong 24h gần nhất
    @Query("SELECT s FROM SensorData s WHERE s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SensorData> findRecentData(@Param("since") LocalDateTime since);
}
