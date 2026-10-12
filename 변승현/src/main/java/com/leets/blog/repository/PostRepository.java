package com.leets.blog.repository;

import com.leets.blog.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p from Post p join fetch p.user where p.deletedAt is null "
            + "and (p.isPrivate = false or p.user.id = :userId) order by p.id desc")
    List<Post> findVisiblePosts(@Param("userId") Long userId);
}
