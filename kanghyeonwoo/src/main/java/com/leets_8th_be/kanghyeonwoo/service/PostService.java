package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PageResponse;
import com.leets_8th_be.kanghyeonwoo.util.Pagination;
import com.leets_8th_be.kanghyeonwoo.dto.PostSummaryResponse;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
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

}
