package com.example.iot_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ActionHistoryViewController {

    @GetMapping("/action-history")
    public String actionHistoryPage() {
        return "action-history"; // Return static HTML file
    }
}
