package com.example.demo.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    java.util.Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
