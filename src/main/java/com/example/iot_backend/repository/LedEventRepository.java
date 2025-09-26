// TRONG LedEventRepository.java - SỬA TẤT CẢ native queries:

package com.example.iot_backend.repository;

import com.example.iot_backend.model.LedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LedEventRepository extends JpaRepository<LedEvent, Long> {

    // =================== BASIC PAGINATION ===================

    Page<LedEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT e FROM LedEvent e WHERE e.ledNumber = :ledNumber ORDER BY e.createdAt DESC")
    Page<LedEvent> findByLedNumberOrderByCreatedAtDesc(@Param("ledNumber") Integer ledNumber, Pageable pageable);

    // =================== FIXED NATIVE QUERIES - NO JPA ORDERING CONFLICT ===================

    /**
     * Find by hour and minute - FIXED: Handle ordering properly
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute",
            nativeQuery = true)
    Page<LedEvent> findByHourAndMinute(@Param("hour") Integer hour, @Param("minute") Integer minute, Pageable pageable);

    /**
     * Find by hour, minute and LED number - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND " +
            "led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND " +
                    "led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByHourMinuteAndLedNumber(
            @Param("hour") Integer hour,
            @Param("minute") Integer minute,
            @Param("ledNumber") Integer ledNumber,
            Pageable pageable);

    /**
     * Find by exact hour, minute, second - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second",
            nativeQuery = true)
    Page<LedEvent> findByHourMinuteAndSecond(
            @Param("hour") Integer hour,
            @Param("minute") Integer minute,
            @Param("second") Integer second,
            Pageable pageable);

    /**
     * Find by exact hour, minute, second and LED number - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second AND " +
            "led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second AND " +
                    "led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByHourMinuteSecondAndLedNumber(
            @Param("hour") Integer hour,
            @Param("minute") Integer minute,
            @Param("second") Integer second,
            @Param("ledNumber") Integer ledNumber,
            Pageable pageable);





    // =================== TIME_FORMAT METHODS - FIXED ===================

    /**
     * Search by time pattern using TIME_FORMAT - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "TIME_FORMAT(created_at, '%H:%i') = :timePattern " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i') = :timePattern",
            nativeQuery = true)
    Page<LedEvent> findByTimePattern(@Param("timePattern") String timePattern, Pageable pageable);

    /**
     * Search by time pattern and LED number - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "TIME_FORMAT(created_at, '%H:%i') = :timePattern AND " +
            "led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i') = :timePattern AND " +
                    "led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByTimePatternAndLedNumber(@Param("timePattern") String timePattern, @Param("ledNumber") Integer ledNumber, Pageable pageable);

    /**
     * Search by exact time pattern (with seconds) - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern",
            nativeQuery = true)
    Page<LedEvent> findByExactTimePattern(@Param("timePattern") String timePattern, Pageable pageable);

    /**
     * Search by exact time pattern and LED number - FIXED
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern AND " +
            "led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern AND " +
                    "led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByExactTimePatternAndLedNumber(@Param("timePattern") String timePattern, @Param("ledNumber") Integer ledNumber, Pageable pageable);

    // =================== DEBUG METHODS ===================

    @Query(value = "SELECT * FROM led_events WHERE " +
            "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern " +
            "ORDER BY created_at DESC LIMIT 10",
            nativeQuery = true)
    List<LedEvent> testTimeSearch(@Param("timePattern") String timePattern);

    // =================== DEPRECATED METHODS ===================

    @Deprecated
    @Query(value = "SELECT * FROM led_events ORDER BY created_at DESC LIMIT 50", nativeQuery = true)
    List<LedEvent> findTop50ByOrderByCreatedAtDesc();

    @Deprecated
    List<LedEvent> findAllByOrderByCreatedAtDesc();

    @Deprecated
    List<LedEvent> findByLedNumberOrderByCreatedAtDesc(Integer ledNumber);

// THÊM VÀO LedEventRepository.java - Date filtering methods:

    /**
     * Find by specific date (full day) - NATIVE SQL
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "DATE(created_at) = :date " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE DATE(created_at) = :date",
            nativeQuery = true)
    Page<LedEvent> findByDate(@Param("date") String date, Pageable pageable);

    /**
     * Find by specific date and LED number - NATIVE SQL
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "DATE(created_at) = :date AND led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "DATE(created_at) = :date AND led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByDateAndLedNumber(@Param("date") String date, @Param("ledNumber") Integer ledNumber, Pageable pageable);

    /**
     * Find by date range - NATIVE SQL
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "DATE(created_at) BETWEEN :startDate AND :endDate " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "DATE(created_at) BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Page<LedEvent> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate, Pageable pageable);

    /**
     * Find by date range and LED number - NATIVE SQL
     */
    @Query(value = "SELECT * FROM led_events WHERE " +
            "DATE(created_at) BETWEEN :startDate AND :endDate AND led_number = :ledNumber " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM led_events WHERE " +
                    "DATE(created_at) BETWEEN :startDate AND :endDate AND led_number = :ledNumber",
            nativeQuery = true)
    Page<LedEvent> findByDateRangeAndLedNumber(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("ledNumber") Integer ledNumber, Pageable pageable);
}
