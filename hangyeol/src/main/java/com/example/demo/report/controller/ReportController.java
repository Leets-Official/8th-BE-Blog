package com.example.demo.report.controller;

import com.example.demo.global.apiPayload.ApiResponse;
import com.example.demo.global.apiPayload.code.GeneralSuccessCode;
import com.example.demo.report.dto.ReportReqDTO;
import com.example.demo.report.dto.ReportResDTO;
import com.example.demo.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class ReportController implements ReportControllerDocs {

    private final ReportService reportService;

    @Override
    @PostMapping("/post/{postId}/report")
    public ApiResponse<ReportResDTO.PostReportResponse> reportPost(@PathVariable Long postId,
                                                  @RequestBody ReportReqDTO.PostReportRequest request){

        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,reportService.postReport(postId,request));
    }

    @Override
    @PostMapping("/comment/{commentId}/report")
    public ApiResponse<ReportResDTO.CommentReportResponse> reportComment(@PathVariable Long commentId,
                                                                   @RequestBody ReportReqDTO.CommentReportRequest request){

        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,reportService.commentReport(commentId,request));
    }

    @Override
    @PatchMapping("/post/{postId}/report/{reporterId}/status")
    public ApiResponse<ReportResDTO.PostReportStatusResponse> updatePostReportStatus(@PathVariable Long postId,
                                                                                     @PathVariable Long reporterId,
                                                                                     @RequestBody ReportReqDTO.PostReportStatusRequest request) {
        return ApiResponse.onSuccess(
                GeneralSuccessCode.SUCCESS_CODE,reportService.updateReportStatus(postId,reporterId,request.status()));
    }
}
