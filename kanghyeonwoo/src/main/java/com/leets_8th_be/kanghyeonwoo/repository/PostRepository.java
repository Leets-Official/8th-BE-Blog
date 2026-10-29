package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id")
    Optional<Post> findForUpdate(@Param("id") Long id);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);
    Page<Post> findByStatusAndNormalizedTitleContaining(PostStatus status, String keyword, Pageable pageable);

    List<Post> findByUserId(Long userId);

    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Post> findByNormalizedTitleContaining(String keyword, Pageable pageable);
}
