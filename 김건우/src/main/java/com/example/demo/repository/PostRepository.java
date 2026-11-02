package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 작성자(user)를 한 번에 JOIN해서 가져옴 → 목록에서 글마다 쿼리가 나가는 N+1 방지
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Post> findAll(Pageable pageable);

    // [4주차] 숨김 글을 제외한 목록 조회
    @EntityGraph(attributePaths = "user")
    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    // [4주차] 상태 변경 / 채택처럼 "읽기 → 검사 → 쓰기" 흐름에서 동시 요청을 직렬화하기 위한 행 잠금
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdForUpdate(@Param("postId") Long postId);
}
