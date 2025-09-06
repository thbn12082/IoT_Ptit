package com.example.iot_backend.dto;

public class LedControlRequest {
    private String deviceId; // led1, led2, led3
    private boolean state; // true/false

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
