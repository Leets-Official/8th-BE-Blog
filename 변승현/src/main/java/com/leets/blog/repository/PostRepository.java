package com.leets.blog.repository;

import com.leets.blog.entity.Post;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p from Post p join fetch p.user where p.deletedAt is null "
            + "and p.status = com.leets.blog.entity.PostStatus.ACTIVE "
            + "and (p.isPrivate = false or p.user.id = :userId) order by p.id desc")
    List<Post> findVisiblePosts(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Post p where p.id = :postId")
    Optional<Post> findForUpdateById(@Param("postId") Long postId);
}
