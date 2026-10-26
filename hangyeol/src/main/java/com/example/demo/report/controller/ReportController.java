package com.example.demo.report.controller;

import com.example.demo.global.apiPayload.ApiResponse;
import com.example.demo.global.apiPayload.code.GeneralSuccessCode;
import com.example.demo.report.dto.ReportReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}/reports")
    public ApiResponse<> reportPost(@PathVariable Long postId,
                                    @RequestBody ReportReqDTO.PostReportRequest requet){

        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,reportService.postReport(postId));
    }


}
