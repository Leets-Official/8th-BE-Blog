package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 작성자(user)를 한 번에 JOIN해서 가져옴 → 목록에서 글마다 쿼리가 나가는 N+1 방지
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Post> findAll(Pageable pageable);
}