package com.example.demo.domain.assignment.controller;

import com.example.demo.domain.assignment.dto.request.RepeatStringRequest;
import com.example.demo.domain.assignment.dto.response.RepeatStringResponse;
import com.example.demo.domain.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/string")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/repeat")
    public ResponseEntity<RepeatStringResponse> repeat(@Valid @RequestBody RepeatStringRequest request) {
        return ResponseEntity.ok(assignmentService.repeat(request));
    }
}
