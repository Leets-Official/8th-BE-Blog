package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndPostId(Long reporterId, Long postId);

    boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId);

    void deleteByPost(Post post);

    void deleteByCommentPost(Post post);
}