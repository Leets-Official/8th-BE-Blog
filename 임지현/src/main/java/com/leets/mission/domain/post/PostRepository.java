package com.leets.mission.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByMemberIdAndTitleAndCreatedAtAfter(Long memberId, String title, LocalDateTime createdAt);
}