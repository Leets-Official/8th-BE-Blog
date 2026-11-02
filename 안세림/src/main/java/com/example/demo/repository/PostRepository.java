package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByStatusAndTitleContaining(PostStatus status, String keyword, Pageable pageable);
}