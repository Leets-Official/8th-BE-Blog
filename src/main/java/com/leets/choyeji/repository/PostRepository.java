package com.leets.choyeji.repository;

import com.leets.choyeji.entity.Post;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Override
    @EntityGraph(attributePaths = "member")
    Page<Post> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "member")
    Optional<Post> findById(Long id);
}
