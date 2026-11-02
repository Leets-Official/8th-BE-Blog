package com.example.demo.domain.post;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostStateRepository extends JpaRepository<PostState, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PostState> findByPostId(Long postId);
}
