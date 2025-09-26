// Tạo class mới: SensorDataPageResponse.java
package com.example.iot_backend.dto;

import com.example.iot_backend.model.SensorData;
import java.time.LocalDateTime;
import java.util.List;

public class SensorDataPageResponse {
    private List<SensorData> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean first;
    private boolean last;
    private String search;
    private LocalDateTime start;
    private LocalDateTime end;

    // Constructors
    public SensorDataPageResponse() {}

    public SensorDataPageResponse(List<SensorData> content, int currentPage,
                                  int totalPages, long totalElements, int size,
                                  boolean first, boolean last, String search) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.size = size;
        this.first = first;
        this.last = last;
        this.search = search;
    }

    // Getters and setters
    public List<SensorData> getContent() { return content; }
    public void setContent(List<SensorData> content) { this.content = content; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
}
