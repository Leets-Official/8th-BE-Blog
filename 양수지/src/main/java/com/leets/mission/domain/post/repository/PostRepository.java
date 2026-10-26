package com.leets.mission.domain.post.repository;

import com.leets.mission.domain.post.entity.Post;
import com.leets.mission.domain.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 특정 상태의 게시글 목록 조회 (User를 함께 조회해 N+1 문제 방지)
    @EntityGraph(attributePaths = "user")
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);
}
