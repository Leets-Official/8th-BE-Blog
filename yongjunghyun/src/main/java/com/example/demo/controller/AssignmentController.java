package com.example.demo.controller;

import com.example.demo.dto.RepeatRequest;
import com.example.demo.dto.RepeatResponse;
import com.example.demo.service.AssignmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/string/repeat")
    public RepeatResponse repeatString(@RequestBody RepeatRequest request) {
        return assignmentService.repeatString(request);
    }
}