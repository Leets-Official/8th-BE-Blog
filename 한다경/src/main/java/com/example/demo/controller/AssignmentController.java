package com.example.demo.controller;

import com.example.demo.dto.request.RepeatStringRequest;
import com.example.demo.dto.response.RepeatStringResponse;
import com.example.demo.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return assignmentService.healthCheck();
    }

    @PostMapping("/string/repeat")
    public RepeatStringResponse repeatString(
            @Valid @RequestBody RepeatStringRequest request
    ) {
        return assignmentService.repeatString(request.getValue());
    }
}
