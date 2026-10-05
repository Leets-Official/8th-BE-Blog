package com.example.demo.domain.assignment.service;

import com.example.demo.domain.assignment.dto.request.RepeatStringRequest;
import com.example.demo.domain.assignment.dto.response.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    @Override
    public RepeatStringResponse repeat(RepeatStringRequest request) {
        return RepeatStringResponse.of(request.text());
    }
}
