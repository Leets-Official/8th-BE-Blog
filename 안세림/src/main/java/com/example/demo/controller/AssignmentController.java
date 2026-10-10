package com.example.demo.controller;

import com.example.demo.dto.RepeatStringRequest;
import com.example.demo.dto.RepeatStringResponse;
import com.example.demo.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/health")
    public String health() {
        return assignmentService.healthCheck();
    }

    @PostMapping("/string/repeat")
    public RepeatStringResponse repeatString(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeatString(request.getValue());
    }
}