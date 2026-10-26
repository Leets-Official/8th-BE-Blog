package com.leets_8th_be.kanghyeonwoo.service;

import com.leets_8th_be.kanghyeonwoo.dto.PostSummaryResponse;
import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import com.leets_8th_be.kanghyeonwoo.util.SearchNormalization;
import com.leets_8th_be.kanghyeonwoo.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostSearchService {
    private final PostService posts;
    private final SearchTrackingService tracking;

    // PostService finishes its read transaction before tracking begins, so a waiting
    // search writer never holds a second connection from the post lookup.
    public PageResponse<PostSummaryResponse> getPosts(int page, int size, String input, SearchSession session) {
        if (page < 1 || size < 1 || size > 100) throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        String keyword = SearchNormalization.normalize(input);
        PageResponse<PostSummaryResponse> results = posts.getPosts(page, size, keyword);
        if (page == 1 && !keyword.isEmpty()) tracking.record(keyword, session.key(), !results.items().isEmpty());
        return results;
    }
}
