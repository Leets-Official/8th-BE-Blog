package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    @Query("select c.post.id from Comment c where c.id = :id")
    Optional<Long> findPostId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Comment c where c.id = :id")
    Optional<Comment> findForUpdate(@Param("id") Long id);

    List<Comment> findByPostId(Long postId);
    List<Comment> findByUserId(Long userId);
}
