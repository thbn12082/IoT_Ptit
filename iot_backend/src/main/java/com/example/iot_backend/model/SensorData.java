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

    @Column(name = "lux")
    private Integer lux;

    @Column(name = "light_raw")
    private Integer lightRaw;

    @Column(name = "light_level")
    private Integer lightLevel;

    @Column(name = "sensor")
    private String sensor;

    @Column(name = "mac")
    private String mac;

    @Column(name = "uptime")
    private Integer uptime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public SensorData() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor với parameters
    public SensorData(Double temperature, Double humidity, Integer lux,
            Integer lightRaw, Integer lightLevel, String sensor, String mac, Integer uptime) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.lux = lux;
        this.lightRaw = lightRaw;
        this.lightLevel = lightLevel;
        this.sensor = sensor;
        this.mac = mac;
        this.uptime = uptime;
        this.createdAt = LocalDateTime.now();
    }

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

    public Integer getLux() {
        return lux;
    }

    public void setLux(Integer lux) {
        this.lux = lux;
    }

    public Integer getLightRaw() {
        return lightRaw;
    }

    public void setLightRaw(Integer lightRaw) {
        this.lightRaw = lightRaw;
    }

    public Integer getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(Integer lightLevel) {
        this.lightLevel = lightLevel;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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
                ", lux=" + lux +
                ", lightRaw=" + lightRaw +
                ", sensor='" + sensor + '\'' +
                ", mac='" + mac + '\'' +
                ", uptime=" + uptime +
                ", createdAt=" + createdAt +
                '}';
    }
}
