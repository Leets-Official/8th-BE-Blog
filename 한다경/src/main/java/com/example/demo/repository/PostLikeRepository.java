package com.example.demo.repository;
import com.example.demo.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberIdAndPostId(Long memberId, Long targetId);
    long countByPostId(Long targetId);
    long deleteByMemberIdAndPostId(Long memberId, Long targetId);
}
