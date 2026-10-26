package com.example.demo.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = "member")
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = "member")
    @Query("select p from Post p left join PostState state on state.post = p " +
            "where p.id = :id and p.deletedAt is null " +
            "and (state.visibility is null or state.visibility <> com.example.demo.domain.post.PostVisibility.HIDDEN)")
    Optional<Post> findVisibleById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :id and p.deletedAt is null")
    Optional<Post> findActiveByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            select p.id as postId, p.title as title, m.name as author,
                   p.createdAt as createdAt, count(c.id) as commentCount
            from Post p join p.member m
            left join p.comments c on c.deletedAt is null
            left join PostState state on state.post = p
            where p.deletedAt is null
              and (state.visibility is null or state.visibility <> com.example.demo.domain.post.PostVisibility.HIDDEN)
            group by p.id, p.title, m.name, p.createdAt
            """, countQuery = "select count(p) from Post p left join PostState state on state.post = p " +
            "where p.deletedAt is null and (state.visibility is null or state.visibility <> com.example.demo.domain.post.PostVisibility.HIDDEN)")
    Page<PostSummary> findActivePosts(Pageable pageable);

    interface PostSummary {
        Long getPostId();
        String getTitle();
        String getAuthor();
        LocalDateTime getCreatedAt();
        long getCommentCount();
    }
}
