package com.leets.blog.domain.post.repository;

import com.leets.blog.domain.post.entity.Post;
import com.leets.blog.domain.post.web.dto.response.PostSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 댓글 수
    String COMMENT_COUNT = "(select count(c) from Comment c where c.post = p and c.deletedAt is null)";

    // 목록 조회 공통 부분: 작성자 이름과 댓글 수를 쿼리 한 번으로 가져옴 (정렬 기준마다 ORDER BY만 다름)
    String SUMMARY_QUERY =
            "select new com.leets.blog.domain.post.web.dto.response.PostSummaryResponse("
            + "p.id, p.title, m.name, p.createdAt, " + COMMENT_COUNT + ") "
            + "from Post p "
            + "join p.member m "
            + "where p.deletedAt is null ";

    String SUMMARY_COUNT_QUERY = "select count(p) from Post p where p.deletedAt is null";

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);

    @Query(value = SUMMARY_QUERY + "order by p.createdAt desc, p.id desc",
            countQuery = SUMMARY_COUNT_QUERY)
    Page<PostSummaryResponse> findLatest(Pageable pageable);

    @Query(value = SUMMARY_QUERY + "order by p.createdAt asc, p.id asc",
            countQuery = SUMMARY_COUNT_QUERY)
    Page<PostSummaryResponse> findOldest(Pageable pageable);

    @Query(value = SUMMARY_QUERY + "order by " + COMMENT_COUNT + " desc, p.createdAt desc, p.id desc",
            countQuery = SUMMARY_COUNT_QUERY)
    Page<PostSummaryResponse> findPopular(Pageable pageable);
}
