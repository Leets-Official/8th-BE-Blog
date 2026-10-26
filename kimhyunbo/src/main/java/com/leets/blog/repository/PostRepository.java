package com.leets.blog.repository;

import com.leets.blog.entity.Post;
import com.leets.blog.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndTitleContaining(PostStatus status, String keyword, Pageable pageable);

    Optional<Post> findByIdAndStatus(Long id, PostStatus status);
}
