package com.example.iot_backend.service;

import com.example.iot_backend.model.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Gửi cập nhật sensor data realtime
    public void sendSensorUpdate(SensorData sensorData) {
        try {
            logger.info("📡 Sending sensor update via WebSocket: {}", sensorData.getId());

            messagingTemplate.convertAndSend("/topic/sensors", sensorData);

        } catch (Exception e) {
            logger.error("❌ Error sending WebSocket update: {}", e.getMessage(), e);
        }
    }

    // Gửi device status update
    public void sendDeviceUpdate(String deviceMac, boolean isOnline) {
        try {
            String status = "{\"mac\":\"" + deviceMac + "\",\"online\":" + isOnline + "}";

            messagingTemplate.convertAndSend("/topic/devices", status);

        } catch (Exception e) {
            logger.error("❌ Error sending device update: {}", e.getMessage(), e);
        }
    }

    // Gửi LED status update
    public void sendLedUpdate(String deviceMac, int ledNumber, boolean state) {
        try {
            String ledStatus = "{\"mac\":\"" + deviceMac + "\",\"led" + ledNumber + "\":" + state + "}";

            messagingTemplate.convertAndSend("/topic/leds", ledStatus);

        } catch (Exception e) {
            logger.error("❌ Error sending LED update: {}", e.getMessage(), e);
        }
    }
}
