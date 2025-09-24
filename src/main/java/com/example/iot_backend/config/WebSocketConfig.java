package com.example.iot_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//WebSocket là giao thức truyền thông full-duplex (hai chiều) trên một kết nối duy nhất giữa client và server. Khác với HTTP request-response truyền thống, WebSocket cho phép server chủ động gửi dữ liệu tới client mà không cần client phải request.
//Real-time Updates: Dữ liệu sensor (nhiệt độ, độ ẩm) thay đổi liên tục cần được hiển thị ngay lập tức trên dashboard
//
//Bidirectional Communication: Dashboard có thể gửi lệnh điều khiển LED, server có thể push trạng thái thiết bị ngược lại
//
//Hiệu suất cao: Không cần polling liên tục như REST API, giảm tải server và bandwidth
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // @Configuration: Đánh dấu đây là Spring configuration class
    //
    // @EnableWebSocketMessageBroker: Kích hoạt STOMP over WebSocket messaging
    //
    // WebSocketMessageBrokerConfigurer: Interface để customize WebSocket
    // configuration
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic");

        // Application destination prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    // enableSimpleBroker("/topic"): Tạo in-memory message broker cho các topic bắt
    // đầu với /topic
    //
    // setApplicationDestinationPrefixes("/app"): Messages gửi tới destinations bắt
    // đầu với /app sẽ được route tới các @MessageMapping methods

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for client connections
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    // Chức năng:
    //
    // addEndpoint("/ws"): Tạo WebSocket endpoint tại URL /ws
    //
    // setAllowedOriginPatterns("*"): Cho phép tất cả origins kết nối (development
    // mode)
    //
    // withSockJS(): Bật SockJS fallback khi browser không support WebSocket
}

// 1. Real-time Dashboard Updates
// Luồng dữ liệu Sensor:
//
// IoT Device → MQTT → MqttService →WebSocketService → "/topic/sensor-data"
// →Dashboard (Live Update)
// Frontend JavaScript sẽ subscribe như sau:
// stompClient.subscribe('/topic/sensor-data', function (message) {
// // Update temperature, humidity, light values
// updateSensorDisplay(JSON.parse(message.body));
// });
// 2. Bidirectional LED Control
// Gửi lệnh từ Dashboard:
// Dashboard → "/app/led-control" → WebSocketController →LedEventService → MQTT
// → IoT Device
// Nhận feedback trạng thái:
// IoT Device → MQTT → "/topic/led-status" → Dashboard
// Tương Tác với Các Thành Phần Khác
// With MQTT System
// WebSocketConfig hoạt động song song với MqttConfig:
//
// MQTT: Giao tiếp với IoT devices
//
// WebSocket: Giao tiếp với web dashboard
//
// Bridge: WebSocketService làm cầu nối giữa hai protocols
//
// With Frontend Dashboard
// Các trang HTML trong static/ sẽ:
//
// Connect: var socket = new SockJS('/ws');
//
// Subscribe: stompClient.subscribe('/topic/sensor-data', callback);
//
// Send Commands: stompClient.send("/app/led-control", {},
// JSON.stringify(command));
//
// With Spring Services
// WebSocketController: Handle incoming /app/* messages
//
// WebSocketService: Broadcast outgoing /topic/* messages
//
// SensorDataService: Process sensor data before broadcasting

// WebSocketConfig này tạo nền tảng cho real-time IoT dashboard, cho phép người
// dùng monitor sensor data và control devices một cách mượt mà và tức thì mà
// không cần refresh page hay polling liên tục.