package com.leets.blog.controller;

import com.leets.blog.dto.RepeatStringRequest;
import com.leets.blog.dto.RepeatStringResponse;
import com.leets.blog.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public String health() {
        return assignmentService.health();
    }

    @PostMapping(value = "/string/repeat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RepeatStringResponse repeat(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeat(request.value());
    }
}
