package com.example.iot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IotBackendApplication {

	public static void main(String[] args) {
		System.out.println("🚀 Starting IoT Backend Server...");
		SpringApplication.run(IotBackendApplication.class, args);
		System.out.println("✅ IoT Backend Server started successfully!");
		System.out.println("🌐 WEB: http://localhost:8081/");
	}

}
