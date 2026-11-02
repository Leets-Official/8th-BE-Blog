package com.example.demo.report.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
@Schema(description = "댓글 신고 ID (댓글 ID + 신고자 ID)")
@Getter
@Embeddable
public class ReportCommentId implements Serializable {

    @Schema(description = "신고된 댓글 ID", example = "1")
    private Long commentId;
    @Schema(description = "신고자 ID", example = "2")
    private Long reporterId;

    protected ReportCommentId() {}

    public ReportCommentId(Long commentId, Long reporterId) {
        this.commentId = commentId;
        this.reporterId = reporterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportCommentId)) return false;

        ReportCommentId that = (ReportCommentId) o;
        return Objects.equals(commentId, that.commentId)
                && Objects.equals(reporterId, that.reporterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentId, reporterId);
    }
}