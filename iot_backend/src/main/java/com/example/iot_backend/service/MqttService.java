package com.example.iot_backend.service;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.repository.LedEventRepository;
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

    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final LedEventRepository ledEventRepository;
    private final WebSocketService webSocketService;

    public MqttService(DeviceService deviceService,
                       SensorDataService sensorDataService,
                       WebSocketService webSocketService,
                       LedEventRepository ledEventRepository) {
        this.deviceService = deviceService;
        this.sensorDataService = sensorDataService;
        this.webSocketService = webSocketService;
        this.ledEventRepository = ledEventRepository;
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    @Transactional
    public void handleInbound(Message<String> message) {
        try {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = message.getPayload();

            logger.info("📨 MQTT received: topic={}, payload={}", topic, payload);

            if (topic.startsWith("home/sensors")) {
                processSensorData(payload);
                return;
            }

            // ✅ CHỈ xử lý LED state từ ESP32
            if (topic.startsWith("home/devices/") && topic.contains("/led/") && topic.endsWith("/state")) {
                processLedStateMessage(topic, payload);
                return;
            }

            logger.warn("⚠️ Unhandled MQTT topic: {}", topic);

        } catch (Exception e) {
            logger.error("❌ Error processing MQTT message: {}", e.getMessage(), e);
        }
    }

    // ✅ CHỈ lưu LED STATE events từ ESP32
    private void processLedStateMessage(String topic, String payload) {
        try {
            String[] parts = topic.split("/");
            if (parts.length >= 6 && "led".equals(parts[3])) {
                String mac = parts[2];
                int led = Integer.parseInt(parts[4]);
                boolean state = "1".equals(payload) || "ON".equalsIgnoreCase(payload) || "true".equalsIgnoreCase(payload);

                logger.info("🔄 Processing LED{} STATE from ESP32 - MAC: {}, State: {}", led, mac, state ? "ON" : "OFF");

                // ✅ Cập nhật device service (không lưu event ở đây)
                deviceService.updateLedStateFromMqtt(mac, led, state);

                // ✅ CHỈ LƯU STATE EVENT TỪ ESP32
                LedEvent stateEvent = new LedEvent();
                stateEvent.setDeviceMac(mac);
                stateEvent.setDeviceName("ESP32-" + mac);
                stateEvent.setLocation("Unknown");
                stateEvent.setIsOnline(true);
                stateEvent.setLastSeen(LocalDateTime.now());
                stateEvent.setLedNumber(led);
                stateEvent.setActionType("STATE");  // ✅ CHỈ STATE từ ESP32
                stateEvent.setStateOn(state);
                stateEvent.setTopic(topic);
                stateEvent.setPayload(payload);
                stateEvent.setSource("esp32");      // ✅ CHỈ ESP32 source
                stateEvent.setCreatedAt(LocalDateTime.now());

                LedEvent savedStateEvent = ledEventRepository.saveAndFlush(stateEvent);
                logger.info("✅ ESP32 LED STATE saved: ID={}", savedStateEvent.getId());

                // ✅ Gửi WebSocket update
                if (webSocketService != null) {
                    webSocketService.sendLedUpdate(mac, led, state);
                }

            } else {
                logger.warn("⚠️ Invalid LED state topic format: {}", topic);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing LED state message: {}", e.getMessage(), e);
        }
    }

    private void processSensorData(String jsonPayload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);

            Double temperature = jsonNode.has("temp") ? jsonNode.get("temp").asDouble() : null;
            Double humidity = jsonNode.has("hum") ? jsonNode.get("hum").asDouble() : null;
            Integer lightLevel = jsonNode.has("lux") ? jsonNode.get("lux").asInt() : null;
            String sensorStatus = jsonNode.has("sensor") ? jsonNode.get("sensor").asText() : "UNKNOWN";
            String deviceMac = jsonNode.has("mac") ? jsonNode.get("mac").asText() : "UNKNOWN";
            Long deviceUptime = jsonNode.has("uptime") ? jsonNode.get("uptime").asLong() : 0L;

            logger.info("🌡️ Processing sensor data - Temp: {}°C, Humidity: {}%, Light: {}, MAC: {}",
                    temperature, humidity, lightLevel, deviceMac);

            SensorData sensorData = new SensorData(
                    temperature, humidity, lightLevel,
                    sensorStatus, deviceMac, deviceUptime
            );

            SensorData savedData = sensorDataService.saveSensorData(sensorData);
            logger.info("✅ Saved sensor data with ID: {}", savedData.getId());

            // ✅ Cập nhật device status (KHÔNG lưu event)
            deviceService.updateDeviceStatus(deviceMac, true);

            if (webSocketService != null) {
                webSocketService.sendSensorUpdate(savedData);
            }

        } catch (Exception e) {
            logger.error("❌ Error processing sensor data: {}", e.getMessage(), e);
        }
    }
}
