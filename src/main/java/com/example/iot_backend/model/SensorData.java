package com.example.iot_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "temperature")
    private Double temperature;

    @NotNull
    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "light_level")
    private Integer lightLevel;

    @Column(name = "uptime")
    private Integer uptime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

/// ///////////////////////////////////////////////////////////////////////////////////// tạo constructor tự động cập nhật thời gian khi khởi tạo
    public SensorData() {
        this.createdAt = LocalDateTime.now();
    }


    public SensorData(Double temperature, Double humidity, Integer lightLevel, Integer uptime) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.lightLevel = lightLevel;
        this.uptime = uptime;
        this.createdAt = LocalDateTime.now();
    }
//    Chức năng: Tự động set timestamp khi tạo entity mới, đảm bảo mọi sensor reading đều có thời gian chính xác.




    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Integer getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(Integer lightLevel) {
        this.lightLevel = lightLevel;
    }

    public Integer getUptime() {
        return uptime;
    }

    public void setUptime(Integer uptime) {
        this.uptime = uptime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", uptime=" + uptime +
                ", createdAt=" + createdAt +
                '}';
    }
}
