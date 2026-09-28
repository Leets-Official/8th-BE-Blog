package com.example.demo.controller;

import com.example.demo.dto.RepeatStringRequest;
import com.example.demo.dto.RepeatStringResponse;
import com.example.demo.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/string/repeat")
    public RepeatStringResponse repeat(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeat(request.value());
    }

}
