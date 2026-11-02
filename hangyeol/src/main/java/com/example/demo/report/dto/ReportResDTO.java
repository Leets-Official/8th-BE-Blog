package com.example.demo.report.dto;

import com.example.demo.report.entity.ReportCommentId;
import com.example.demo.report.entity.ReportPostId;
import com.example.demo.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ReportResDTO {
    @Schema(description = "게시글 신고 응답")
    public record PostReportResponse (
            @Schema(description = "신고 ID (게시글 ID + 신고자 ID)")
            ReportPostId reportPostId
    ){}

    @Schema(description = "댓글 신고 응답")
    public record CommentReportResponse (
            @Schema(description = "신고 ID (댓글 ID + 신고자 ID)")
            ReportCommentId reportCommentId
    ){}

    @Schema(description = "게시글 신고 처리 응답")
    public record PostReportStatusResponse(
            @Schema(description = "신고 ID (게시글 ID + 신고자 ID)")
            ReportPostId reportPostId,
            @Schema(description = "변경된 신고 상태", example = "ACCEPTED")
            ReportStatus status
    ) {}
}
