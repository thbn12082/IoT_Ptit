package com.example.iot_backend.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class DeviceInfo {
    private String macAddress;
    private String deviceName;
    private String location;
    private Boolean isOnline;
    private LocalDateTime lastSeen;

    // LED states từ events gần nhất
    private Map<Integer, Boolean> ledStates = new HashMap<>();

    // Constructors
    public DeviceInfo() {}

    public DeviceInfo(String macAddress, String deviceName, String location) {
        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.location = location;
        this.isOnline = false;

        // Khởi tạo LED states = false
        this.ledStates.put(1, false);
        this.ledStates.put(2, false);
        this.ledStates.put(3, false);
    }

    // Getters và Setters
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public Map<Integer, Boolean> getLedStates() { return ledStates; }
    public void setLedStates(Map<Integer, Boolean> ledStates) { this.ledStates = ledStates; }

    // Helper methods cho LED states
    public Boolean getLed1State() { return ledStates.get(1); }
    public void setLed1State(Boolean state) { ledStates.put(1, state); }

    public Boolean getLed2State() { return ledStates.get(2); }
    public void setLed2State(Boolean state) { ledStates.put(2, state); }

    public Boolean getLed3State() { return ledStates.get(3); }
    public void setLed3State(Boolean state) { ledStates.put(3, state); }
}
