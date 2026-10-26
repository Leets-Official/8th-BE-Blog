package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.CommentResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PageResponse;
import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.CommentStatus;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.util.Pagination;
import com.leets_8th_be.kanghyeonwoo.util.TextSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository comments;
    private final PostRepository posts;

    public PageResponse<CommentResponse> getComments(Long postId, int page, int size, String keyword) {
        var pageable = Pagination.newestFirst(page, size);
        if (postId < 1) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        posts.findById(postId).filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        Specification<Comment> criteria = (root, query, cb) -> cb.equal(root.get("post").get("id"), postId);
        if (keyword != null && !keyword.isBlank()) {
            criteria = criteria.and((root, query, cb) -> cb.and(
                    cb.equal(root.get("status"), CommentStatus.ACTIVE), cb.isFalse(root.get("isDeleted")),
                    TextSearch.contains(cb, root.get("content"), keyword.strip())));
        }
        return PageResponse.from(comments.findAll(criteria, pageable).map(CommentResponse::from));
    }
}
