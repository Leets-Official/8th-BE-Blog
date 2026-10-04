package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.dto.RepeatStringResponse;

@Service
public class AssignmentService {

    public String healthCheck() {
        return "OK";
    }

    public RepeatStringResponse repeatString(String value) {
        return new RepeatStringResponse(value, value);
    }
}