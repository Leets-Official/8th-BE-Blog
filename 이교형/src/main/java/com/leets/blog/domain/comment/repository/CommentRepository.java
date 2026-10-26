package com.leets.blog.domain.comment.repository;

import com.leets.blog.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 수정/삭제 시 단건 조회
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);

    // 글의 댓글 목록 (작성자를 함께 조회해 N+1 방지, 정렬은 Pageable로 지정)
    @EntityGraph(attributePaths = "member")
    Page<Comment> findByPostIdAndDeletedAtIsNull(Long postId, Pageable pageable);

    // 게시글 삭제 시 그 글의 댓글을 한 번에 soft delete (이미 삭제된 댓글은 원래 삭제 시각 유지)
    @Modifying
    @Query("""
            update Comment c
            set c.deletedAt = :deletedAt, c.updatedAt = :deletedAt
            where c.post.id = :postId and c.deletedAt is null
            """)
    int softDeleteAllByPostId(@Param("postId") Long postId, @Param("deletedAt") LocalDateTime deletedAt);
}
