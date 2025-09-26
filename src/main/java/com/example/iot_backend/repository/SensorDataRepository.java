package com.example.iot_backend.repository;

import com.example.iot_backend.model.SensorData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // =================== BASIC PAGINATION ===================

    Page<SensorData> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // =================== GENERAL SEARCH METHODS (MISSING ONES) ===================

    /**
     * Multi-field search - search across all fields
     */
    @Query("SELECT s FROM SensorData s WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "STR(s.id) LIKE %:search% OR " +
            "STR(s.temperature) LIKE %:search% OR " +
            "STR(s.humidity) LIKE %:search% OR " +
            "STR(s.lightLevel) LIKE %:search%) " +
            "ORDER BY s.createdAt DESC")
    Page<SensorData> findByMultiFieldSearch(@Param("search") String search, Pageable pageable);

    /**
     * Search by ID containing
     */
    @Query("SELECT s FROM SensorData s WHERE STR(s.id) LIKE %:search% ORDER BY s.createdAt DESC")
    Page<SensorData> findByIdContaining(@Param("search") String search, Pageable pageable);

    /**
     * Search by Temperature containing
     */
    @Query("SELECT s FROM SensorData s WHERE STR(s.temperature) LIKE %:search% ORDER BY s.createdAt DESC")
    Page<SensorData> findByTemperatureContaining(@Param("search") String search, Pageable pageable);

    /**
     * Search by Humidity containing
     */
    @Query("SELECT s FROM SensorData s WHERE STR(s.humidity) LIKE %:search% ORDER BY s.createdAt DESC")
    Page<SensorData> findByHumidityContaining(@Param("search") String search, Pageable pageable);

    /**
     * Search by Light Level containing
     */
    @Query("SELECT s FROM SensorData s WHERE STR(s.lightLevel) LIKE %:search% ORDER BY s.createdAt DESC")
    Page<SensorData> findByLightLevelContaining(@Param("search") String search, Pageable pageable);

    // =================== TIME/DATE SEARCH METHODS ===================

    /**
     * Find by specific date (full day) - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "DATE(created_at) = :date " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE DATE(created_at) = :date",
            nativeQuery = true)
    Page<SensorData> findByDate(@Param("date") String date, Pageable pageable);

    /**
     * Find by date range - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "DATE(created_at) BETWEEN :startDate AND :endDate " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "DATE(created_at) BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Page<SensorData> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate, Pageable pageable);

    /**
     * Find by hour and minute pattern - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute",
            nativeQuery = true)
    Page<SensorData> findByHourAndMinute(@Param("hour") Integer hour, @Param("minute") Integer minute, Pageable pageable);

    /**
     * Find by exact hour, minute, second - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "HOUR(created_at) = :hour AND MINUTE(created_at) = :minute AND SECOND(created_at) = :second",
            nativeQuery = true)
    Page<SensorData> findByHourMinuteAndSecond(@Param("hour") Integer hour, @Param("minute") Integer minute, @Param("second") Integer second, Pageable pageable);

    /**
     * Search by time pattern using TIME_FORMAT - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "TIME_FORMAT(created_at, '%H:%i') = :timePattern " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i') = :timePattern",
            nativeQuery = true)
    Page<SensorData> findByTimePattern(@Param("timePattern") String timePattern, Pageable pageable);

    /**
     * Search by exact time pattern (with seconds) - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "TIME_FORMAT(created_at, '%H:%i:%s') = :timePattern",
            nativeQuery = true)
    Page<SensorData> findByExactTimePattern(@Param("timePattern") String timePattern, Pageable pageable);

    // =================== ADVANCED RANGE SEARCH (Optional, if needed later) ===================

    /**
     * Find by temperature range - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "temperature BETWEEN :minTemp AND :maxTemp " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "temperature BETWEEN :minTemp AND :maxTemp",
            nativeQuery = true)
    Page<SensorData> findByTemperatureRange(@Param("minTemp") Double minTemp, @Param("maxTemp") Double maxTemp, Pageable pageable);

    /**
     * Find by humidity range - NATIVE SQL
     */
    @Query(value = "SELECT * FROM sensor_data WHERE " +
            "humidity BETWEEN :minHumidity AND :maxHumidity " +
            "ORDER BY created_at DESC",
            countQuery = "SELECT COUNT(*) FROM sensor_data WHERE " +
                    "humidity BETWEEN :minHumidity AND :maxHumidity",
            nativeQuery = true)
    Page<SensorData> findByHumidityRange(@Param("minHumidity") Double minHumidity, @Param("maxHumidity") Double maxHumidity, Pageable pageable);

    // =================== DEPRECATED METHODS (For backward compatibility) ===================

    @Deprecated
    @Query(value = "SELECT * FROM sensor_data ORDER BY created_at DESC LIMIT 50", nativeQuery = true)
    List<SensorData> findTop50ByOrderByCreatedAtDesc();

    @Deprecated
    List<SensorData> findAllByOrderByCreatedAtDesc();
}
