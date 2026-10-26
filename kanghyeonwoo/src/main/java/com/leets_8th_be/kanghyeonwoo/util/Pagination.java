package com.leets_8th_be.kanghyeonwoo.util;

import com.leets_8th_be.kanghyeonwoo.exception.CustomException;
import com.leets_8th_be.kanghyeonwoo.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Pagination {
    private Pagination() {}

    public static Pageable newestFirst(int page, int size) {
        if (page < 1 || size < 1 || size > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    }
}
