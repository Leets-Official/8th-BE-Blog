package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}
