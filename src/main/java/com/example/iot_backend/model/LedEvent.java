package com.example.iot_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "led_events")  // Correct table name
public class LedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "led_number")  // Correct column name
    private Integer ledNumber;

    @Column(name = "state_on")  // Correct column name
    private String state;  // Will store "ON" or "OFF"

    @Column(name = "created_at")  // Correct column name
    private LocalDateTime createdAt;

    // Constructors
    public LedEvent() {
        this.createdAt = LocalDateTime.now();
    }

    public LedEvent(Integer ledNumber, String state) {
        this.ledNumber = ledNumber;
        this.state = state;
        this.createdAt = LocalDateTime.now();
    }

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

    // Helper methods for boolean state
    public Boolean getStateOn() {
        return "ON".equals(this.state);
    }

    public void setStateOn(Boolean stateOn) {
        this.state = (stateOn != null && stateOn) ? "ON" : "OFF";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LedEvent{" +
                "id=" + id +
                ", ledNumber=" + ledNumber +
                ", state='" + state + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
