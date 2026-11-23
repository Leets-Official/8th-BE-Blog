package com.example.demo.report.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
@Schema(description = "게시글 신고 ID (게시글 ID + 신고자 ID)")
@Getter
@Embeddable
public class ReportPostId implements Serializable {

    @Schema(description = "신고된 게시글 ID", example = "1")
    private Long postId;
    @Schema(description = "신고자 ID", example = "2")
    private Long reporterId;

    protected ReportPostId() {}

    public ReportPostId(Long postId, Long reporterId) {
        this.postId = postId;
        this.reporterId = reporterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportPostId)) return false;

        ReportPostId that = (ReportPostId) o;
        return Objects.equals(postId, that.postId)
                && Objects.equals(reporterId, that.reporterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(postId, reporterId);
    }
}