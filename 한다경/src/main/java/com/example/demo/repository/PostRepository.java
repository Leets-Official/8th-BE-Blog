package com.example.demo.repository;

import com.example.demo.domain.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = "member")
    List<Post> findAllByDeletedAtIsNullAndIsPrivateFalseAndIsHiddenFalseOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "member")
    Optional<Post> findById(Long id);
}
