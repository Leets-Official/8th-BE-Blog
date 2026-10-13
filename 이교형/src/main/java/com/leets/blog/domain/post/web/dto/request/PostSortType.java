package com.leets.blog.domain.post.web.dto.request;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;

import java.util.Arrays;

public enum PostSortType {
    LATEST,     // 최신순
    OLDEST,     // 오래된순
    POPULAR;    // 댓글 많은 순

    // "latest" 같은 쿼리 값을 enum으로 변환, 없는 값이면 400
    public static PostSortType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SORT_TYPE));
    }
}
