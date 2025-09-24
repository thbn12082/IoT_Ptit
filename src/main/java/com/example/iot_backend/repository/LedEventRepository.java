package com.example.iot_backend.repository;

import com.example.iot_backend.model.LedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LedEventRepository extends JpaRepository<LedEvent, Long> {

    //Lấy lịch sử điều khiển của một LED cụ thể, sắp xếp giảm dần
    List<LedEvent> findByLedNumberOrderByCreatedAtDesc(Integer ledNumber);

    // Tìm trạng thái hiện tại của LED
    @Query("SELECT e FROM LedEvent e WHERE e.ledNumber = :ledNumber ORDER BY e.createdAt DESC")
    List<LedEvent> findLatestLedState(@Param("ledNumber") int ledNumber);

    //Lấy 50 events gần nhất của tất cả LEDs
    List<LedEvent> findTop50ByOrderByCreatedAtDesc();
    List<LedEvent> findAllByOrderByCreatedAtDesc();
}
