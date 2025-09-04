package com.example.iot_backend.service;

import com.example.iot_backend.dto.DeviceInfo;
import com.example.iot_backend.gateway.MqttGateway;
import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);

    private final LedEventRepository ledEventRepository;
    private final MqttGateway mqttGateway;

    public DeviceService(LedEventRepository ledEventRepository, MqttGateway mqttGateway) {
        this.ledEventRepository = ledEventRepository;
        this.mqttGateway = mqttGateway;
    }


    // ✅ Điều khiển LED
    @Transactional
    public LedEvent controlLed(String macAddress, int ledNumber, boolean state) {
        logger.info("🎯 Controlling LED{} for device {}: {}", ledNumber, macAddress, state ? "ON" : "OFF");

        // ✅ CHỈ GỬI MQTT COMMAND
        mqttGateway.sendToMqtt("home/lamps/" + ledNumber, state ? "1" : "0");
        logger.info("📤 MQTT command sent: home/lamps/{} -> {}", ledNumber, state ? "1" : "0");

        return null; // hoặc return một object khác thay vì LedEvent
    }

    // ✅ Cập nhật device status - KHÔNG lưu DEVICE_STATUS event
    @Transactional
    public void updateDeviceStatus(String macAddress, boolean isOnline) {
        logger.info("📱 Device {} status: {}", macAddress, isOnline ? "ONLINE" : "OFFLINE");
        logger.info("✅ Device status logged: {} = {}", macAddress, isOnline ? "ONLINE" : "OFFLINE");
    }

    // ✅ Cập nhật LED state từ MQTT - KHÔNG lưu STATE event ở đây
    @Transactional
    public void updateLedStateFromMqtt(String macAddress, int ledNumber, boolean state) {
        logger.info("📡 LED{} state from MQTT: {} = {}", ledNumber, macAddress, state);

        // ❌ BỎ PHẦN LƯU STATE EVENT (sẽ lưu trong MqttService)
        // LedEvent stateEvent = new LedEvent();
        // ...
        // ledEventRepository.saveAndFlush(stateEvent);

        // ✅ CHỈ LOG THÔI
        logger.info("✅ LED{} state updated: {}", ledNumber, state);
    }



    // ✅ Lấy tất cả devices từ led_events
    public List<DeviceInfo> getAllDevices() {
        List<String> deviceMacs = ledEventRepository.findAllDeviceMacs();
        return deviceMacs.stream()
                .map(this::getDeviceInfo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ✅ Lấy thông tin device từ events gần nhất
    public DeviceInfo getDeviceInfo(String macAddress) {
        List<LedEvent> latestEvents = ledEventRepository.findLatestDeviceInfo(macAddress);

        if (latestEvents.isEmpty()) {
            return null;
        }

        LedEvent latestEvent = latestEvents.get(0);
        DeviceInfo deviceInfo = new DeviceInfo(
                macAddress,
                latestEvent.getDeviceName() != null ? latestEvent.getDeviceName() : "ESP32-" + macAddress,
                latestEvent.getLocation() != null ? latestEvent.getLocation() : "Unknown"
        );

        deviceInfo.setIsOnline(latestEvent.getIsOnline());
        deviceInfo.setLastSeen(latestEvent.getLastSeen());

        // ✅ Lấy LED states gần nhất
        Map<Integer, Boolean> ledStates = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            List<LedEvent> ledEvents = ledEventRepository.findLatestLedState(macAddress, i);
            ledStates.put(i, ledEvents.isEmpty() ? false : ledEvents.get(0).getStateOn());
        }
        deviceInfo.setLedStates(ledStates);

        return deviceInfo;
    }



    // ✅ SỬA: Statistics với parameter
    public long countOnlineDevices() {
        // ✅ Tính threshold = 10 phút trước thời điểm hiện tại
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
        return ledEventRepository.countOnlineDevices(threshold);
    }

    public List<DeviceInfo> getOnlineDevices() {
        return getAllDevices().stream()
                .filter(device -> Boolean.TRUE.equals(device.getIsOnline()))
                .collect(Collectors.toList());
    }

    // ✅ THÊM: Lấy events gần đây
    public List<LedEvent> getRecentEvents(int minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return ledEventRepository.findRecentEvents(threshold);
    }
}
