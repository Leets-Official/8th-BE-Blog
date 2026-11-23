package com.example.demo.report.dto;

import com.example.demo.report.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class ReportReqDTO {
    @Schema(description = "게시글 신고 요청")
    public record PostReportRequest (
        @Schema(description = "신고자 ID", example = "2")
        Long reporterId,
        @Schema(description = "신고 사유 (500자 이하)", example = "광고성 게시글입니다.", maxLength = 500)
        String reason
    ){}

    @Schema(description = "댓글 신고 요청")
    public record CommentReportRequest (
        @Schema(description = "신고자 ID", example = "2")
        Long reporterId,
        @Schema(description = "신고 사유 (500자 이하)", example = "욕설이 포함된 댓글입니다.", maxLength = 500)
        String reason

    ){}

    @Schema(description = "게시글 신고 처리 요청")
    public record PostReportStatusRequest (
        @Schema(description = "변경할 신고 상태 (ACCEPTED 또는 REJECTED)", example = "ACCEPTED")
        ReportStatus status
    ) {}
}
