package com.example.demo.controller;

import com.example.demo.controller.docs.ReportControllerDocs;

import com.example.demo.dto.request.ReportCreateRequest;
import com.example.demo.dto.response.ReportResponse;
import com.example.demo.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportControllerDocs {
    private final ReportService reports;

    @Override
    @PostMapping("/posts/{postId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse reportPost(@PathVariable Long postId, Principal principal,
                                     @Valid @RequestBody ReportCreateRequest request) {
        return reports.reportPost(postId, Long.valueOf(principal.getName()), request);
    }

    @Override
    @PostMapping("/comments/{commentId}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse reportComment(@PathVariable Long commentId, Principal principal,
                                        @Valid @RequestBody ReportCreateRequest request) {
        return reports.reportComment(commentId, Long.valueOf(principal.getName()), request);
    }

    @Override
    @PostMapping("/reports/{reportId}/approve")
    public ReportResponse approve(@PathVariable Long reportId, Principal principal) {
        return reports.approve(reportId, Long.valueOf(principal.getName()));
    }
}
