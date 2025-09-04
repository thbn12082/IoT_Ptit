package com.example.iot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IotBackendApplication {

	public static void main(String[] args) {
		System.out.println("🚀 Starting IoT Backend Server...");
		SpringApplication.run(IotBackendApplication.class, args);
		System.out.println("✅ IoT Backend Server started successfully!");
		System.out.println("📡 MQTT Broker: test.mosquitto.org:1883");
		System.out.println("🌐 REST API: http://localhost:8081/api");
		System.out.println("🔌 WebSocket: ws://localhost:8081/ws");
	}

}
