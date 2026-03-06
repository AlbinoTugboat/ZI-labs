package com.example.informationprotection.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BasicController {

    @GetMapping("/public/ping")
    public Map<String, String> publicPing() {
        return Map.of("message", "public ok");
    }

    @GetMapping("/user/me")
    public Map<String, String> userMe(Authentication authentication) {
        return Map.of(
                "message", "user zone",
                "username", authentication.getName()
        );
    }
}

