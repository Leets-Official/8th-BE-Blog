package com.example.week1.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }
}
