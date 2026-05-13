// src/main/java/com/pos/controller/TestController.java
package com.pos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("status", "Backend is running!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("status", "No authentication needed");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secure")
    public ResponseEntity<Map<String, String>> secureEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a secure endpoint");
        response.put("status", "Authentication required");
        return ResponseEntity.ok(response);
    }
}