package com.leets.mission.domain.comment.repository;

import com.leets.mission.domain.comment.entity.Comment;
import com.leets.mission.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 삭제 시 달린 댓글을 한 번의 쿼리로 함께 삭제
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteAllByPost(@Param("post") Post post);
}
