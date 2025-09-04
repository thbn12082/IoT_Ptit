package com.example.iot_backend.repository;

import com.example.iot_backend.model.LedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedEventRepository extends JpaRepository<LedEvent, Long> {

    // Events cơ bản
    List<LedEvent> findTop50ByDeviceMacOrderByCreatedAtDesc(String deviceMac);
    List<LedEvent> findTop50ByOrderByCreatedAtDesc();
    List<LedEvent> findByActionTypeOrderByCreatedAtDesc(String actionType);

    // ✅ Lấy thông tin thiết bị từ event gần nhất
    @Query("SELECT e FROM LedEvent e WHERE e.deviceMac = :mac AND e.actionType IN ('STATE', 'DEVICE_STATUS') ORDER BY e.createdAt DESC")
    List<LedEvent> findLatestDeviceInfo(@Param("mac") String mac);

    // ✅ Lấy danh sách MAC addresses của tất cả thiết bị
    @Query("SELECT DISTINCT e.deviceMac FROM LedEvent e")
    List<String> findAllDeviceMacs();

    // ✅ Lấy LED state gần nhất cho từng LED của device
    @Query("SELECT e FROM LedEvent e WHERE e.deviceMac = :mac AND e.ledNumber = :ledNumber AND e.actionType = 'STATE' ORDER BY e.createdAt DESC")
    List<LedEvent> findLatestLedState(@Param("mac") String mac, @Param("ledNumber") int ledNumber);

    // ✅ SỬA: Đếm devices online với parameter thay vì INTERVAL
    @Query("SELECT COUNT(DISTINCT e.deviceMac) FROM LedEvent e WHERE e.isOnline = true AND e.createdAt > :threshold")
    long countOnlineDevices(@Param("threshold") LocalDateTime threshold);

    // ✅ THÊM: Lấy events gần nhất theo thời gian
    @Query("SELECT e FROM LedEvent e WHERE e.createdAt > :threshold ORDER BY e.createdAt DESC")
    List<LedEvent> findRecentEvents(@Param("threshold") LocalDateTime threshold);

    // ✅ THÊM: Native query cho MySQL nếu cần
    @Query(value = "SELECT COUNT(DISTINCT device_mac) FROM led_events WHERE is_online = true AND created_at > (NOW() - INTERVAL 10 MINUTE)", nativeQuery = true)
    long countOnlineDevicesNative();
}
