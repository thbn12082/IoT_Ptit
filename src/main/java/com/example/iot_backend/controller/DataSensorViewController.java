package com.example.iot_backend.controller;

import com.example.iot_backend.model.SensorData;
import com.example.iot_backend.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.format.DateTimeFormatter;

@Controller
public class DataSensorViewController {

    @Autowired
    private SensorDataService sensorDataService;

    // Serve HTML page
    @GetMapping("/data-sensor")
    public String dataSensorPage() {
        return "data-sensor"; // Return static HTML file
    }
}