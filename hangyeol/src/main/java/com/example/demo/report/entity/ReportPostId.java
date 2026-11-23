package com.example.demo.report.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
@Getter
@Embeddable
public class ReportPostId implements Serializable {

    private Long postId;
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