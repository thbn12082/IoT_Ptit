package com.example.iot_backend.service;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;  // ADDED: Missing import
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class LedEventService {

    @Autowired
    private LedEventRepository repository;

    // =================== BASIC CRUD METHODS ===================

    @Transactional
    public LedEvent save(int ledNumber, boolean stateOn) {
        LedEvent event = new LedEvent();
        event.setLedNumber(ledNumber);
        event.setStateOn(stateOn);
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }

    public List<LedEvent> getRecentEvents() {
        return repository.findTop50ByOrderByCreatedAtDesc();
    }

    public List<LedEvent> getEventsByLed(int ledNumber) {
        return repository.findByLedNumberOrderByCreatedAtDesc(ledNumber);
    }

    public long getTotalRecords() {
        return repository.count();
    }

    // =================== MAIN PAGINATION METHOD ===================

    public Page<LedEvent> getLedEventsPaginated(int page, int size, String search, String deviceFilter, String timeFilter) {
        // Use unsorted for native queries to avoid JPA ordering conflicts
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());

        System.out.println("=== ADVANCED TIME/DATE SEARCH ===");
        System.out.println("Page: " + page + ", Size: " + size);
        System.out.println("Device Filter: " + deviceFilter);
        System.out.println("Time Filter: '" + timeFilter + "'");

        // Parse time/date pattern if provided
        TimePattern timePattern = null;
        if (timeFilter != null && !timeFilter.trim().isEmpty()) {
            try {
                timePattern = parseTimePattern(timeFilter.trim());
                System.out.println("Parsed Pattern: " + timePattern);
            } catch (Exception e) {
                System.err.println("Failed to parse pattern: " + timeFilter + " - " + e.getMessage());
                return Page.empty();
            }
        }

        // Apply filters based on pattern type
        Page<LedEvent> result = null;

        try {
            if (timePattern != null) {
                result = searchWithTimePattern(timePattern, deviceFilter, pageable);
            } else if (deviceFilter != null && !deviceFilter.equals("all")) {
                // Device filter only
                Integer ledNumber = Integer.parseInt(deviceFilter);
                System.out.println("Searching by device only: LED " + ledNumber);
                result = repository.findByLedNumberOrderByCreatedAtDesc(ledNumber, pageable);
            } else {
                // No filters - return all
                System.out.println("No filters - returning all records");
                result = repository.findAllByOrderByCreatedAtDesc(pageable);
            }

        } catch (Exception e) {
            System.err.println("Error in search: " + e.getMessage());
            e.printStackTrace();
            result = Page.empty();
        }

        // Debug results
        if (result != null) {
            System.out.println("Query returned " + result.getContent().size() + " results out of " + result.getTotalElements() + " total");

            // Print first few results for debugging
            result.getContent().stream().limit(3).forEach(event -> {
                System.out.println("Result: ID=" + event.getId() +
                        ", LED=" + event.getLedNumber() +
                        ", State=" + event.getState() +
                        ", Time=" + event.getCreatedAt());
            });
        }

        return result != null ? result : Page.empty();
    }

    // =================== PRIVATE HELPER METHODS ===================

    private Page<LedEvent> searchWithTimePattern(TimePattern timePattern, String deviceFilter, Pageable pageable) {
        boolean hasDeviceFilter = deviceFilter != null && !deviceFilter.equals("all");
        Integer ledNumber = hasDeviceFilter ? Integer.parseInt(deviceFilter) : null;

        switch (timePattern.type) {
            case FULL_DATE:
                System.out.println("Searching for date: " + timePattern.dateString +
                        (hasDeviceFilter ? " on LED " + ledNumber : " on all devices"));
                return hasDeviceFilter
                        ? repository.findByDateAndLedNumber(timePattern.dateString, ledNumber, pageable)
                        : repository.findByDate(timePattern.dateString, pageable);

            case DATE_RANGE:
                System.out.println("Searching for date range: " + timePattern.startDateString + " to " + timePattern.endDateString +
                        (hasDeviceFilter ? " on LED " + ledNumber : " on all devices"));
                return hasDeviceFilter
                        ? repository.findByDateRangeAndLedNumber(timePattern.startDateString, timePattern.endDateString, ledNumber, pageable)
                        : repository.findByDateRange(timePattern.startDateString, timePattern.endDateString, pageable);

            case HOUR_MINUTE:
                String timePatternStr = String.format("%02d:%02d", timePattern.hour, timePattern.minute);
                System.out.println("Searching for time pattern: " + timePatternStr +
                        (hasDeviceFilter ? " on LED " + ledNumber : " on all devices"));
                return hasDeviceFilter
                        ? repository.findByTimePatternAndLedNumber(timePatternStr, ledNumber, pageable)
                        : repository.findByTimePattern(timePatternStr, pageable);

            case HOUR_MINUTE_SECOND:
                String exactTimeStr = String.format("%02d:%02d:%02d", timePattern.hour, timePattern.minute, timePattern.second);
                System.out.println("Searching for exact time: " + exactTimeStr +
                        (hasDeviceFilter ? " on LED " + ledNumber : " on all devices"));
                return hasDeviceFilter
                        ? repository.findByExactTimePatternAndLedNumber(exactTimeStr, ledNumber, pageable)
                        : repository.findByExactTimePattern(exactTimeStr, pageable);

            default:
                throw new IllegalArgumentException("Unsupported time pattern type: " + timePattern.type);
        }
    }

    private TimePattern parseTimePattern(String timeFilter) throws DateTimeParseException {
        TimePattern pattern = new TimePattern();

        try {
            // FLEXIBLE DATE RANGE: Support "6/9/2025-8/9/2025", "06/09/2025-08/09/2025", etc.
            if (timeFilter.contains("-") && timeFilter.matches("\\d{1,2}/\\d{1,2}/\\d{4}\\s*-\\s*\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] dates = timeFilter.split("-");
                String startDateStr = dates[0].trim();
                String endDateStr = dates[1].trim();

                // Try multiple date formats for flexibility
                LocalDate startDate = parseFlexibleDate(startDateStr);
                LocalDate endDate = parseFlexibleDate(endDateStr);

                DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                pattern.type = TimePatternType.DATE_RANGE;
                pattern.startDateString = startDate.format(sqlFormatter);
                pattern.endDateString = endDate.format(sqlFormatter);
                return pattern;
            }

            // FLEXIBLE SINGLE DATE: Support "6/9/2025", "06/09/2025", "6/09/2025", etc.
            if (timeFilter.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                LocalDate date = parseFlexibleDate(timeFilter);
                DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                pattern.type = TimePatternType.FULL_DATE;
                pattern.dateString = date.format(sqlFormatter);
                return pattern;
            }

            // Try time with seconds: "13:28:45"
            if (timeFilter.matches("\\d{1,2}:\\d{2}:\\d{2}")) {
                String[] parts = timeFilter.split(":");
                pattern.type = TimePatternType.HOUR_MINUTE_SECOND;
                pattern.hour = Integer.parseInt(parts[0]);
                pattern.minute = Integer.parseInt(parts[1]);
                pattern.second = Integer.parseInt(parts[2]);

                validateTimeComponents(pattern.hour, pattern.minute, pattern.second, timeFilter);
                return pattern;
            }

            // Try time without seconds: "13:28"
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
                        ". Supported formats: HH:mm, HH:mm:ss, d/M/yyyy, dd/MM/yyyy, d/M/yyyy-d/M/yyyy",
                timeFilter, 0);
    }

    // THÊM helper method để parse flexible date formats
    private LocalDate parseFlexibleDate(String dateStr) throws DateTimeParseException {
        dateStr = dateStr.trim();

        // List of possible date formats to try
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("d/M/yyyy"),    // "6/9/2025"
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),  // "06/09/2025"
                DateTimeFormatter.ofPattern("d/MM/yyyy"),   // "6/09/2025"
                DateTimeFormatter.ofPattern("dd/M/yyyy"),   // "06/9/2025"
                DateTimeFormatter.ofPattern("d-M-yyyy"),    // "6-9-2025" (alternative separator)
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),  // "06-09-2025"
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),  // "2025/09/06" (ISO-ish)
                DateTimeFormatter.ofPattern("yyyy-MM-dd")   // "2025-09-06" (ISO standard)
        };

        // Try each formatter until one works
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                System.out.println("Successfully parsed '" + dateStr + "' using format: " + formatter.toString());
                return date;
            } catch (DateTimeParseException e) {
                // Continue trying other formats
                continue;
            }
        }

        // If no format worked, throw exception
        throw new DateTimeParseException(
                "Unable to parse date: " + dateStr +
                        ". Supported formats: d/M/yyyy, dd/MM/yyyy, d/MM/yyyy, dd/M/yyyy",
                dateStr, 0);
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

    // =================== BACKWARD COMPATIBILITY ===================

    public Page<LedEvent> getLedEventsPaginated(int page, int size, String search) {
        return getLedEventsPaginated(page, size, search, "all", null);
    }

    // =================== HELPER CLASSES AND ENUMS ===================

    enum TimePatternType {
        HOUR_MINUTE,        // "13:28"
        HOUR_MINUTE_SECOND, // "13:28:45"
        FULL_DATE,          // "24/09/2025"
        DATE_RANGE          // "24/09/2025-26/09/2025"
    }

    static class TimePattern {
        TimePatternType type;
        Integer hour;
        Integer minute;
        Integer second;
        String dateString;      // For single date searches (YYYY-MM-DD format)
        String startDateString; // For date range searches
        String endDateString;   // For date range searches

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

    // =================== UTILITY METHODS ===================

    public static String getDeviceName(int ledNumber) {
        switch (ledNumber) {
            case 1: return "Đèn";
            case 2: return "Quạt";
            case 3: return "Điều hòa";
            default: return "LED " + ledNumber;
        }
    }

    public static String getDeviceIcon(int ledNumber) {
        switch (ledNumber) {
            case 1: return "💡";
            case 2: return "🌀";
            case 3: return "❄️";
            default: return "💡";
        }
    }
}
