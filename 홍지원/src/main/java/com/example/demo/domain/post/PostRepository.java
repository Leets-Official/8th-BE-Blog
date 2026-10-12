package com.example.demo.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = "member")
    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @Query(value = """
            select p.id as postId, p.title as title, m.name as author,
                   p.createdAt as createdAt, count(c.id) as commentCount
            from Post p join p.member m
            left join p.comments c on c.deletedAt is null
            where p.deletedAt is null
            group by p.id, p.title, m.name, p.createdAt
            """, countQuery = "select count(p) from Post p where p.deletedAt is null")
    Page<PostSummary> findActivePosts(Pageable pageable);

    interface PostSummary {
        Long getPostId();
        String getTitle();
        String getAuthor();
        LocalDateTime getCreatedAt();
        long getCommentCount();
    }
}
