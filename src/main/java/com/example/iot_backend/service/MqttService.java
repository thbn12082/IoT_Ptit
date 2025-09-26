package com.example.iot_backend.service;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.repository.LedEventRepository;
import com.example.iot_backend.repository.SensorDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class MqttService {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorDataRepository sensorDataRepository;
    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final LedEventRepository ledEventRepository;
    private final WebSocketService webSocketService;

    // FIXED: Constructor with proper closing brace
    public MqttService(DeviceService deviceService,
                       SensorDataService sensorDataService,
                       WebSocketService webSocketService,
                       LedEventRepository ledEventRepository,
                       SensorDataRepository sensorDataRepository) {
        this.deviceService = deviceService;
        this.sensorDataService = sensorDataService;
        this.webSocketService = webSocketService;
        this.ledEventRepository = ledEventRepository;
        this.sensorDataRepository = sensorDataRepository;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleInbound(Message<?> message) throws Exception {
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload(); // FIXED: Cast to String
        logger.info("📨 MQTT received: topic={}, payload={}", topic, payload);

        try {
            // Handle sensor data
            if ("home/sensors".equals(topic)) {
                processSensorData(payload);
                return;
            }

            // Xử lý state message từ ESP32
            if (topic != null && topic.startsWith("home/devices/") && topic.endsWith("/state")) {
                // Format: home/devices/{mac}/led/{number}/state
                String[] parts = topic.split("/");
                if (parts.length == 6) {
                    try {
                        String mac = parts[2];
                        int ledNumber = Integer.parseInt(parts[4]);
                        boolean state = "1".equals(payload);
                        processLedStateMessage(ledNumber, mac, state, payload);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid LED number in topic: {}", topic);
                        throw e;
                    }
                }
                return;
            }

            // Xử lý command message cho LED control
            if (topic != null && topic.startsWith("home/lamps/")) {
                String[] parts = topic.split("/");
                if (parts.length == 3) {
                    try {
                        int ledNumber = Integer.parseInt(parts[2]);
                        boolean state = "1".equals(payload);
                        processLedCommandMessage(ledNumber, state);
                    } catch (NumberFormatException e) {
                        logger.error("Invalid LED number in topic: {}", topic);
                        throw e;
                    }
                }
                return;
            }

            logger.warn("⚠️ Unhandled MQTT topic: {}", topic);

        } catch (Exception e) {
            logger.error("❌ Error handling MQTT message: {}", e.getMessage(), e);
            throw e; // Re-throw để Spring Integration có thể xử lý
        }
    }

    @Transactional
    protected void processLedStateMessage(int ledNumber, String mac, boolean state, String payload) throws Exception {
        try {
            logger.info("🔄 Processing LED {} STATE - State: {}", ledNumber, state ? "ON" : "OFF");

            // Create and save LED event with only essential information
            LedEvent stateEvent = new LedEvent();
            stateEvent.setLedNumber(ledNumber);
            stateEvent.setStateOn(state);
            stateEvent.setCreatedAt(LocalDateTime.now()); // ADDED: Ensure timestamp is set

            LedEvent savedStateEvent = ledEventRepository.save(stateEvent);
            logger.info("✅ ESP32 LED {} STATE saved: ID={}", ledNumber, savedStateEvent.getId());

            // Send WebSocket update
            if (webSocketService != null) {
                webSocketService.sendLedUpdate(mac, ledNumber, state);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing LED {} state message for device {}: {}",
                    ledNumber, mac, e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
        }
    }

    private void processLedCommandMessage(int ledNumber, boolean state) throws Exception {
        try {
            logger.info("🔄 Processing LED {} COMMAND - Requested State: {}",
                    ledNumber, state ? "ON" : "OFF");

            // WebSocket update for command acknowledgment
            if (webSocketService != null) {
                webSocketService.sendLedCommandAck(ledNumber, state);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing LED {} command: {}", ledNumber, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    protected void processSensorData(String payload) throws Exception {
        try {
            JsonNode node = objectMapper.readTree(payload);

            // Create new sensor data object with only essential fields
            SensorData sensorData = new SensorData();
            sensorData.setTemperature(node.get("temp").asDouble());
            sensorData.setHumidity(node.get("hum").asDouble());

            // Calculate light level from raw value (0-4095) to percentage (0-100)
            int lightLevel = (int) ((1.0 - (node.get("light_raw").asDouble() / 4095.0)) * 100);
            sensorData.setLightLevel(lightLevel);
            sensorData.setUptime(node.get("uptime").asInt());
            sensorData.setCreatedAt(LocalDateTime.now());

            // Save to database
            sensorDataRepository.save(sensorData);

            // Send update via WebSocket
            if (webSocketService != null) {
                webSocketService.sendSensorUpdate(sensorData);
            }

            logger.info("✅ Sensor data processed and saved successfully");

        } catch (Exception e) {
            logger.error("❌ Error processing sensor data: {}", e.getMessage(), e);
            throw e; // Re-throw để transaction rollback
        }
    }
}
