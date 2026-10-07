package com.example.demo.service;

import com.example.demo.dto.RepeatRequest;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public String repeatMessage(RepeatRequest request) {
        if (request == null || request.value() == null) {
            return "";
        }
        return request.value().repeat(Math.max(0, request.count()));
    }
}