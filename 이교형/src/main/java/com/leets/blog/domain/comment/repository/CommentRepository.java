package com.leets.blog.domain.comment.repository;

import com.leets.blog.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 수정/삭제 시 단건 조회
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
