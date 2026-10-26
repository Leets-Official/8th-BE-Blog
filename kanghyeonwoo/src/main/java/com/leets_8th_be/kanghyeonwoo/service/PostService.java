package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PageResponse;
import com.leets_8th_be.kanghyeonwoo.util.Pagination;
import com.leets_8th_be.kanghyeonwoo.dto.PostDetailResponse;
import com.leets_8th_be.kanghyeonwoo.dto.PostSummaryResponse;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.util.SearchNormalization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PageResponse<PostSummaryResponse> getPosts(int page, int size, String keyword) {
        Pageable pageable = Pagination.newestFirst(page, size);

        Page<Post> postPage;
        if (!SearchNormalization.normalize(keyword).isEmpty()) {
            postPage = postRepository.findByStatusAndNormalizedTitleContaining(
                    PostStatus.ACTIVE, SearchNormalization.requireKeyword(keyword), pageable);
        } else {
            postPage = postRepository.findByStatus(PostStatus.ACTIVE, pageable);
        }

        return PageResponse.from(postPage.map(PostSummaryResponse::from));
    }

    @Transactional
    public PostDetailResponse updatePost(Long postId, com.leets_8th_be.kanghyeonwoo.dto.PostUpdateRequest request) {
        Post post = postRepository.findForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(request.getAuthorId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_POST_ACCESS);
        }

        post.update(request.getTitle(), request.getContent());
        return PostDetailResponse.from(post);
    }

    @Transactional
    public com.leets_8th_be.kanghyeonwoo.dto.PostDeleteResponse deletePost(Long postId, Long authorId) {
        Post post = postRepository.findForUpdate(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(authorId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_POST_ACCESS);
        }

        postRepository.delete(post);
        return new com.leets_8th_be.kanghyeonwoo.dto.PostDeleteResponse(postId);
    }
}
