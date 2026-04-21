package com.contactmanagement.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // Simple endpoint to confirm the app is running.
    @GetMapping("/")
    public String home() {
        return "Contact Management backend is running.";
    }
}
