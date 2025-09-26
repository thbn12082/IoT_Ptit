package com.example.iot_backend.service;

import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class SensorDataService {

    @Autowired
    private SensorDataRepository repository;

    // =================== NEW PAGINATION METHOD ===================

    public Page<SensorData> getSensorDataPaginated(int page, int size, String search, String searchType, String timeFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());

        System.out.println("=== SENSOR DATA TIME/DATE SEARCH ===");
        System.out.println("Page: " + page + ", Size: " + size);
        System.out.println("Search: '" + search + "', Type: " + searchType);
        System.out.println("Time Filter: '" + timeFilter + "'");

        TimePattern timePattern = null;
        if (timeFilter != null && !timeFilter.trim().isEmpty()) {
            try {
                timePattern = parseTimePattern(timeFilter.trim());
                System.out.println("Parsed Pattern: " + timePattern);
            } catch (Exception e) {
                System.err.println("Failed to parse time pattern: " + timeFilter + " - " + e.getMessage());
                return Page.empty();
            }
        }

        Page<SensorData> result = null;

        try {
            if (timePattern != null) {
                result = searchWithTimePattern(timePattern, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                result = searchWithGeneralQuery(search.trim(), searchType, pageable);
            } else {
                System.out.println("No filters - returning all sensor data");
                result = repository.findAllByOrderByCreatedAtDesc(pageable);
            }

        } catch (Exception e) {
            System.err.println("Error in sensor data search: " + e.getMessage());
            e.printStackTrace();
            result = Page.empty();
        }

        if (result != null) {
            System.out.println("Query returned " + result.getContent().size() + " results out of " + result.getTotalElements() + " total");
        }

        return result != null ? result : Page.empty();
    }

    // =================== COMPATIBILITY METHODS (ADDED) ===================

    /**
     * Get all sensor data without pagination - FOR COMPATIBILITY
     */
    public List<SensorData> getAllSensorData() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get sensor data by date range - FOR COMPATIBILITY
     */
    public List<SensorData> getSensorDataByDateRange(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = start.toLocalDate().format(sqlFormatter);
        String endDate = end.toLocalDate().format(sqlFormatter);

        Pageable unpaged = PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted());
        Page<SensorData> page = repository.findByDateRange(startDate, endDate, unpaged);

        return page.getContent();
    }

    /**
     * Get sensor data by ID - FOR COMPATIBILITY
     */
    public Optional<SensorData> getSensorDataById(Long id) {
        return repository.findById(id);
    }

    /**
     * Get sensor data by temperature range - FOR COMPATIBILITY
     */
    public List<SensorData> getSensorDataByTemperatureRange(Double minTemp, Double maxTemp) {
        Pageable unpaged = PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted());
        Page<SensorData> page = repository.findByTemperatureRange(minTemp, maxTemp, unpaged);
        return page.getContent();
    }

    /**
     * Get sensor data by humidity range - FOR COMPATIBILITY
     */
    public List<SensorData> getSensorDataByHumidityRange(Double minHumidity, Double maxHumidity) {
        Pageable unpaged = PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted());
        Page<SensorData> page = repository.findByHumidityRange(minHumidity, maxHumidity, unpaged);
        return page.getContent();
    }

    // =================== EXISTING METHODS ===================

    public List<SensorData> getRecentData() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public long getTotalRecords() {
        return repository.count();
    }

    public Page<SensorData> getSensorDataPaginated(int page, int size, String search) {
        return getSensorDataPaginated(page, size, search, "Auto Detect", null);
    }

    // =================== HELPER METHODS ===================

    private Page<SensorData> searchWithTimePattern(TimePattern timePattern, Pageable pageable) {
        switch (timePattern.type) {
            case FULL_DATE:
                System.out.println("Searching sensor data for date: " + timePattern.dateString);
                return repository.findByDate(timePattern.dateString, pageable);

            case DATE_RANGE:
                System.out.println("Searching sensor data for date range: " + timePattern.startDateString + " to " + timePattern.endDateString);
                return repository.findByDateRange(timePattern.startDateString, timePattern.endDateString, pageable);

            case HOUR_MINUTE:
                String timePatternStr = String.format("%02d:%02d", timePattern.hour, timePattern.minute);
                System.out.println("Searching sensor data for time pattern: " + timePatternStr);
                return repository.findByTimePattern(timePatternStr, pageable);

            case HOUR_MINUTE_SECOND:
                String exactTimeStr = String.format("%02d:%02d:%02d", timePattern.hour, timePattern.minute, timePattern.second);
                System.out.println("Searching sensor data for exact time: " + exactTimeStr);
                return repository.findByExactTimePattern(exactTimeStr, pageable);

            default:
                throw new IllegalArgumentException("Unsupported time pattern type: " + timePattern.type);
        }
    }

    private Page<SensorData> searchWithGeneralQuery(String search, String searchType, Pageable pageable) {
        if (searchType == null || "Auto Detect".equals(searchType)) {
            return repository.findByMultiFieldSearch(search, pageable);
        }

        switch (searchType) {
            case "ID":
                return repository.findByIdContaining(search, pageable);
            case "Temperature (°C)":
                return repository.findByTemperatureContaining(search, pageable);
            case "Humidity (%)":
                return repository.findByHumidityContaining(search, pageable);
            case "Light Level":
                return repository.findByLightLevelContaining(search, pageable);
            default:
                return repository.findByMultiFieldSearch(search, pageable);
        }
    }

    private TimePattern parseTimePattern(String timeFilter) throws DateTimeParseException {
        TimePattern pattern = new TimePattern();

        try {
            if (timeFilter.contains("-") && timeFilter.matches("\\d{1,2}/\\d{1,2}/\\d{4}\\s*-\\s*\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] dates = timeFilter.split("-");
                LocalDate startDate = parseFlexibleDate(dates[0].trim());
                LocalDate endDate = parseFlexibleDate(dates[1].trim());

                DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                pattern.type = TimePatternType.DATE_RANGE;
                pattern.startDateString = startDate.format(sqlFormatter);
                pattern.endDateString = endDate.format(sqlFormatter);
                return pattern;
            }

            if (timeFilter.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                LocalDate date = parseFlexibleDate(timeFilter);
                DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                pattern.type = TimePatternType.FULL_DATE;
                pattern.dateString = date.format(sqlFormatter);
                return pattern;
            }

            if (timeFilter.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                String[] parts = timeFilter.split(":");
                pattern.type = TimePatternType.HOUR_MINUTE_SECOND;
                pattern.hour = Integer.parseInt(parts[0]);
                pattern.minute = Integer.parseInt(parts[1]);
                pattern.second = Integer.parseInt(parts[2]);

                validateTimeComponents(pattern.hour, pattern.minute, pattern.second, timeFilter);
                return pattern;
            }

            if (timeFilter.matches("\\d{1,2}:\\d{2}")) {
                String[] parts = timeFilter.split(":");
                pattern.type = TimePatternType.HOUR_MINUTE;
                pattern.hour = Integer.parseInt(parts[0]);
                pattern.minute = Integer.parseInt(parts[1]);

                validateTimeComponents(pattern.hour, pattern.minute, null, timeFilter);
                return pattern;
            }

        } catch (NumberFormatException | DateTimeParseException e) {
            throw new DateTimeParseException("Invalid format: " + e.getMessage(), timeFilter, 0);
        }

        throw new DateTimeParseException(
                "Unable to parse time/date pattern: " + timeFilter +
                        ". Supported formats: HH:mm, HH:mm:ss, d/M/yyyy, d/M/yyyy-d/M/yyyy",
                timeFilter, 0);
    }

    private LocalDate parseFlexibleDate(String dateStr) throws DateTimeParseException {
        dateStr = dateStr.trim();

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        throw new DateTimeParseException("Unable to parse date: " + dateStr, dateStr, 0);
    }

    private void validateTimeComponents(Integer hour, Integer minute, Integer second, String original) throws DateTimeParseException {
        if (hour < 0 || hour > 23) {
            throw new DateTimeParseException("Invalid hour: " + hour, original, 0);
        }
        if (minute < 0 || minute > 59) {
            throw new DateTimeParseException("Invalid minute: " + minute, original, 0);
        }
        if (second != null && (second < 0 || second > 59)) {
            throw new DateTimeParseException("Invalid second: " + second, original, 0);
        }
    }

    // =================== TIME PATTERN CLASSES ===================

    enum TimePatternType {
        HOUR_MINUTE,
        HOUR_MINUTE_SECOND,
        FULL_DATE,
        DATE_RANGE
    }

    static class TimePattern {
        TimePatternType type;
        Integer hour;
        Integer minute;
        Integer second;
        String dateString;
        String startDateString;
        String endDateString;

        @Override
        public String toString() {
            return "TimePattern{" +
                    "type=" + type +
                    ", hour=" + hour +
                    ", minute=" + minute +
                    ", second=" + second +
                    ", dateString='" + dateString + '\'' +
                    ", startDateString='" + startDateString + '\'' +
                    ", endDateString='" + endDateString + '\'' +
                    '}';
        }
    }
}
