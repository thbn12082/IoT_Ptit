package com.example.iot_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "led_events")
public class LedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "led_number", nullable = false)
    private Integer ledNumber;
/// ///////////////////////////////////////////////////////////
    public static final String STATE_ON = "ON";
    public static final String STATE_OFF = "OFF";

    @Column(name = "state_on", nullable = false)
    private String state;

//    Chức năng: Chuyển đổi giữa boolean và string representation cho trạng thái LED, giúp dễ dàng lưu trữ và hiển thị.

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
//    Chức năng: Tự động set thời gian tạo khi entity được persist vào database.
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLedNumber() {
        return ledNumber;
    }

    public void setLedNumber(Integer ledNumber) {
        this.ledNumber = ledNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setStateOn(Boolean stateOn) {
        this.state = stateOn ? STATE_ON : STATE_OFF;
    }

    public Boolean getStateOn() {
        return STATE_ON.equals(this.state);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
//Tự động set thời gian tạo khi entity được persist vào database.
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
