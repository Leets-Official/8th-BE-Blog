package com.example.demo.report.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReportCommentId implements Serializable {

    private Long commentId;
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