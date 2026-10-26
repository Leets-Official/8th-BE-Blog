package com.leets.blog.domain.report.repository;

import com.leets.blog.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 같은 회원이 같은 대상을 이미 신고했는지 확인 (unique 제약의 인덱스 사용)
    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);
}
