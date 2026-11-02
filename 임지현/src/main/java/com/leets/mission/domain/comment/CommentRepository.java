package com.leets.mission.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostIdAndStatusAndDeletedAtIsNullOrderByCreatedAtAsc(
            Long postId,
            CommentStatus status
    );

    Optional<Comment> findByIdAndStatusAndDeletedAtIsNull(
            Long id,
            CommentStatus status
    );

    Optional<Comment> findByIdAndPostIdAndStatusAndDeletedAtIsNull(
            Long id,
            Long postId,
            CommentStatus status
    );

    // 관리자 상태 변경용: HIDDEN 상태여도 조회 가능
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMemberIdAndId(Long memberId, Long commentId);
}