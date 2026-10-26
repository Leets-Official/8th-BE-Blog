package com.leets.mission.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByStatusAndDeletedAtIsNull(
            PostStatus status,
            Pageable pageable
    );

    Optional<Post> findByIdAndStatusAndDeletedAtIsNull(
            Long id,
            PostStatus status
    );

    // 관리자 상태 변경용: HIDDEN 상태여도 조회 가능
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMemberIdAndTitleAndCreatedAtAfterAndDeletedAtIsNull(
            Long memberId,
            String title,
            LocalDateTime createdAt
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Post p 
            SET p.viewCount = p.viewCount + 1 
            WHERE p.id = :postId
                AND p.status = :status
                AND p.deletedAt IS NULL
            """)
    int increaseViewCount(
            @Param("postId") Long postId,
            @Param("status") PostStatus status
    );
}