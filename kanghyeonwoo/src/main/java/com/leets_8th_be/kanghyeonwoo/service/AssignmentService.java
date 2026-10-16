package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringRequest;
import com.leets_8th_be.kanghyeonwoo.dto.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public String getHealthStatus() {
        return "ok";
    }

    public RepeatStringResponse repeatString(RepeatStringRequest request) {
        String value = (request != null) ? request.getValue() : null;
        return RepeatStringResponse.of(value, value);
    }
}
