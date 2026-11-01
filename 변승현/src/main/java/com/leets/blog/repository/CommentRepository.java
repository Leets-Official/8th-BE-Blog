package com.leets.blog.repository;

import com.leets.blog.entity.Comment;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"post", "post.user"})
    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
