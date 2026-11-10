package com.leets_8th_be.kanghyeonwoo.repository;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id")
    Optional<Post> findForUpdate(@Param("id") Long id);

    List<Post> findByUserId(Long userId);
}
