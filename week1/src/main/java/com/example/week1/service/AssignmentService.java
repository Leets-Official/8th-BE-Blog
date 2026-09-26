package com.example.week1.service;

import com.example.week1.dto.request.RepeatStringRequest;
import com.example.week1.dto.response.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public String healthCheck() {
        return "ok";
    }

    public RepeatStringResponse repeatString(RepeatStringRequest request) {
        return new RepeatStringResponse(
                request.getValue(),
                request.getValue()
        );
    }
}
