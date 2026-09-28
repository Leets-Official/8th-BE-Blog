package com.example.demo.service;

import com.example.demo.dto.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public String healthCheck() {
        return "ok";
    }

    public RepeatStringResponse repeatString(String value) {
        return new RepeatStringResponse(value, value);
    }
}