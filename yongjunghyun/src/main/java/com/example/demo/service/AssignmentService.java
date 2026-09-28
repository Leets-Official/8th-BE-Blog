package com.example.demo.service;

import com.example.demo.dto.RepeatRequest;
import com.example.demo.dto.RepeatResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public RepeatResponse repeatString(RepeatRequest request) {
        String repeated = request.getMessage().repeat(request.getCount());
        return new RepeatResponse(repeated);
    }
}