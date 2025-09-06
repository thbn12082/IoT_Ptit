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
            int ledNumber;
            String topic;
            
            // Handle different deviceId formats
            if ("all".equals(request.getDeviceId())) {
                // For "All LEDs" button, we'll control LED 1 and return its event
                // In practice, you might want to send to all LEDs
                ledNumber = 1;
                topic = "home/lamps/1";
                
                // Send commands to all LEDs
                String payload = request.isState() ? "1" : "0";
                mqttGateway.sendToMqtt("home/lamps/1", payload);
                mqttGateway.sendToMqtt("home/lamps/2", payload);
                mqttGateway.sendToMqtt("home/lamps/3", payload);
            } else {
                // Extract LED number from deviceId (led1, led2, led3)
                if (request.getDeviceId().startsWith("led")) {
                    ledNumber = Integer.parseInt(request.getDeviceId().substring(3));
                } else {
                    // Handle "LED 1", "LED 2", "LED 3" format
                    ledNumber = Integer.parseInt(request.getDeviceId().replaceAll("[^0-9]", ""));
                }
                
                // Topic for specific LED control
                topic = "home/lamps/" + ledNumber;
                
                // Send MQTT message with simple payload (just "1" or "0")
                String payload = request.isState() ? "1" : "0";
                mqttGateway.sendToMqtt(topic, payload);
            }

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
