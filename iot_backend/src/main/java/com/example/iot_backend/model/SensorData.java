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

    @NotNull
    @Column(name = "light_level")
    private Integer lightLevel;

    @Column(name = "sensor_status")
    private String sensorStatus;

    @Column(name = "device_mac")
    private String deviceMac;

    @Column(name = "device_uptime")
    private Long deviceUptime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public SensorData() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor với parameters
    public SensorData(Double temperature, Double humidity, Integer lightLevel,
                      String sensorStatus, String deviceMac, Long deviceUptime) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.lightLevel = lightLevel;
        this.sensorStatus = sensorStatus;
        this.deviceMac = deviceMac;
        this.deviceUptime = deviceUptime;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Integer getLightLevel() { return lightLevel; }
    public void setLightLevel(Integer lightLevel) { this.lightLevel = lightLevel; }

    public String getSensorStatus() { return sensorStatus; }
    public void setSensorStatus(String sensorStatus) { this.sensorStatus = sensorStatus; }

    public String getDeviceMac() { return deviceMac; }
    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }

    public Long getDeviceUptime() { return deviceUptime; }
    public void setDeviceUptime(Long deviceUptime) { this.deviceUptime = deviceUptime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", lightLevel=" + lightLevel +
                ", sensorStatus='" + sensorStatus + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
