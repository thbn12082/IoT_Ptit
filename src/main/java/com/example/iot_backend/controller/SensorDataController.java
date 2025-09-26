package com.example.iot_backend.controller;

import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sensor-data")
@CrossOrigin(origins = "*")
public class SensorDataController {

    @Autowired
    private SensorDataService sensorDataService;

    // =================== MAIN PAGINATION ENDPOINT ===================

    @GetMapping("/paginated")
    public ResponseEntity<Map<String, Object>> getSensorDataPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "Auto Detect") String searchType,
            @RequestParam(required = false) String timeFilter) {

        try {
            Page<SensorData> pageData = sensorDataService.getSensorDataPaginated(page, size, search, searchType, timeFilter);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageData.getContent());
            response.put("currentPage", pageData.getNumber());
            response.put("totalPages", pageData.getTotalPages());
            response.put("totalElements", pageData.getTotalElements());
            response.put("size", pageData.getSize());
            response.put("first", pageData.isFirst());
            response.put("last", pageData.isLast());
            response.put("search", search);
            response.put("searchType", searchType);
            response.put("timeFilter", timeFilter);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // =================== COMPATIBILITY ENDPOINTS ===================

    @GetMapping("/all")
    public ResponseEntity<List<SensorData>> getAllSensorData() {
        try {
            List<SensorData> data = sensorDataService.getAllSensorData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SensorData>> getRecentData() {
        try {
            List<SensorData> data = sensorDataService.getRecentData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getTotalRecords() {
        try {
            long count = sensorDataService.getTotalRecords();
            Map<String, Object> response = new HashMap<>();
            response.put("total", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SensorData> getSensorDataById(@PathVariable Long id) {
        try {
            Optional<SensorData> data = sensorDataService.getSensorDataById(id);
            if (data.isPresent()) {
                return ResponseEntity.ok(data.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =================== SPECIFIC SEARCH ENDPOINTS ===================

    @GetMapping("/search/temperature")
    public ResponseEntity<Map<String, Object>> searchByTemperature(
            @RequestParam Double temperature,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // FIXED: Use correct method signature with all 5 parameters
            Page<SensorData> pageData = sensorDataService.getSensorDataPaginated(
                    page, size, temperature.toString(), "Temperature (°C)", null);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageData.getContent());
            response.put("currentPage", pageData.getNumber());
            response.put("totalPages", pageData.getTotalPages());
            response.put("totalElements", pageData.getTotalElements());
            response.put("size", pageData.getSize());
            response.put("first", pageData.isFirst());
            response.put("last", pageData.isLast());
            response.put("searchTerm", temperature.toString());
            response.put("searchType", "Temperature (°C)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search by temperature: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/search/humidity")
    public ResponseEntity<Map<String, Object>> searchByHumidity(
            @RequestParam Double humidity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // FIXED: Use correct method signature with all 5 parameters
            Page<SensorData> pageData = sensorDataService.getSensorDataPaginated(
                    page, size, humidity.toString(), "Humidity (%)", null);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageData.getContent());
            response.put("currentPage", pageData.getNumber());
            response.put("totalPages", pageData.getTotalPages());
            response.put("totalElements", pageData.getTotalElements());
            response.put("size", pageData.getSize());
            response.put("first", pageData.isFirst());
            response.put("last", pageData.isLast());
            response.put("searchTerm", humidity.toString());
            response.put("searchType", "Humidity (%)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search by humidity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/search/light")
    public ResponseEntity<Map<String, Object>> searchByLightLevel(
            @RequestParam Integer lightLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // FIXED: Use correct method signature with all 5 parameters
            Page<SensorData> pageData = sensorDataService.getSensorDataPaginated(
                    page, size, lightLevel.toString(), "Light Level", null);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageData.getContent());
            response.put("currentPage", pageData.getNumber());
            response.put("totalPages", pageData.getTotalPages());
            response.put("totalElements", pageData.getTotalElements());
            response.put("size", pageData.getSize());
            response.put("first", pageData.isFirst());
            response.put("last", pageData.isLast());
            response.put("searchTerm", lightLevel.toString());
            response.put("searchType", "Light Level");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to search by light level: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // =================== RANGE SEARCH ENDPOINTS ===================

    @GetMapping("/search/temperature-range")
    public ResponseEntity<List<SensorData>> searchByTemperatureRange(
            @RequestParam Double minTemp,
            @RequestParam Double maxTemp) {
        try {
            List<SensorData> data = sensorDataService.getSensorDataByTemperatureRange(minTemp, maxTemp);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/humidity-range")
    public ResponseEntity<List<SensorData>> searchByHumidityRange(
            @RequestParam Double minHumidity,
            @RequestParam Double maxHumidity) {
        try {
            List<SensorData> data = sensorDataService.getSensorDataByHumidityRange(minHumidity, maxHumidity);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search/date-range")
    public ResponseEntity<List<SensorData>> searchByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);

            List<SensorData> data = sensorDataService.getSensorDataByDateRange(start, end);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // =================== DEBUG ENDPOINTS ===================

    @GetMapping("/debug/test-search")
    public ResponseEntity<Map<String, Object>> testSearch(
            @RequestParam String search,
            @RequestParam(defaultValue = "Auto Detect") String searchType) {

        try {
            Page<SensorData> result = sensorDataService.getSensorDataPaginated(0, 5, search, searchType, null);

            Map<String, Object> response = new HashMap<>();
            response.put("searchType", searchType);
            response.put("searchTerm", search);
            response.put("found", result.getTotalElements());
            response.put("results", result.getContent());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("searchType", searchType);
            error.put("searchTerm", search);
            return ResponseEntity.ok(error);
        }
    }
}
