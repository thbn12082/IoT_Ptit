package com.example.iot_backend.repository;

import com.example.iot_backend.model.LedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LedEventRepository extends JpaRepository<LedEvent, Long> {

    // Get latest events by LED number
    List<LedEvent> findByLedNumberOrderByCreatedAtDesc(Integer ledNumber);

    // Get latest state for a specific LED
    @Query("SELECT e FROM LedEvent e WHERE e.ledNumber = :ledNumber ORDER BY e.createdAt DESC")
    List<LedEvent> findLatestLedState(@Param("ledNumber") int ledNumber);

    // Get recent events
    List<LedEvent> findTop50ByOrderByCreatedAtDesc();
}
