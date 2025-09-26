// Tạo file: StatsResponse.java
package com.example.iot_backend.dto;

public class StatsResponse {
    private long totalRecords;
    private String status;

    public StatsResponse() {}

    public StatsResponse(long totalRecords, String status) {
        this.totalRecords = totalRecords;
        this.status = status;
    }

    // Getters and setters
    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
