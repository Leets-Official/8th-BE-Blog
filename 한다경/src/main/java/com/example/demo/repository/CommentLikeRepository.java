package com.example.demo.repository;
import com.example.demo.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByMemberIdAndCommentId(Long memberId, Long targetId);
    long countByCommentId(Long targetId);
    long deleteByMemberIdAndCommentId(Long memberId, Long targetId);
}
