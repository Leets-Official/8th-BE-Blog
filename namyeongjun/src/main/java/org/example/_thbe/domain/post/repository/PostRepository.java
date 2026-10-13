package org.example._thbe.domain.post.repository;

import org.example._thbe.domain.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {

    @EntityGraph(attributePaths = {"member"})
    Optional<Post> findWithMemberById(Long postId);

    @Query("SELECT p FROM Post p JOIN FETCH p.member")
    List<Post> findAllWithMember();
}
