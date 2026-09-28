package com.example.demo.domain.assignment.service;

import com.example.demo.domain.assignment.dto.request.RepeatStringRequest;
import com.example.demo.domain.assignment.dto.response.RepeatStringResponse;

public interface AssignmentService {
    // 문자열 2번 반복
    RepeatStringResponse repeat(RepeatStringRequest request);
}
