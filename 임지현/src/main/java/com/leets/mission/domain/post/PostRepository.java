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
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMemberIdAndTitleAndCreatedAtAfterAndDeletedAtIsNull(
            Long memberId,
            String title,
            LocalDateTime createdAt
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId AND p.deletedAt IS NULL")
    int increaseViewCount(@Param("postId") Long postId);
}