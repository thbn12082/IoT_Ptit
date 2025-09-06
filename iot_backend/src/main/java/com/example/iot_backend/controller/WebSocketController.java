package com.example.iot_backend.controller;

import com.example.iot_backend.gateway.MqttGateway;
import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import com.example.iot_backend.dto.LedControlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketController {

    @Autowired
    private MqttGateway mqttGateway;

    @Autowired
    private LedEventRepository ledEventRepository;

    @MessageMapping("/led-control")
    @SendTo("/topic/led-status")
    public LedEvent handleLedControl(LedControlRequest request) {
        try {
            // Extract LED number from deviceId (led1, led2, led3)
            int ledNumber = Character.getNumericValue(request.getDeviceId().charAt(3));

            // Topic for LED control
            String topic = "home/lamps/2";

            // Create JSON payload
            String payload = String.format(
                    "{\"mac\":\"E0:E2:E6:63:21:F0\", \"light_raw\":%d}",
                    request.isState() ? 1 : 0); // Send MQTT message
            mqttGateway.sendToMqtt(topic, payload);

            // Create and save the event
            LedEvent ledEvent = new LedEvent();

            ledEvent.setLedNumber(ledNumber);

            ledEvent.setStateOn(request.isState());

            ledEvent.setCreatedAt(LocalDateTime.now());

            return ledEventRepository.save(ledEvent);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process LED control", e);
        }
    }
}
