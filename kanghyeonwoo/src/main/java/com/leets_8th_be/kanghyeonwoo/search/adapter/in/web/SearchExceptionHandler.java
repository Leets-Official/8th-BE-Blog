package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.search.application.exception.InvalidSearchRequest;
import com.leets_8th_be.kanghyeonwoo.search.application.exception.SearchKeywordNotFound;
import com.leets_8th_be.kanghyeonwoo.search.domain.InvalidSearchKeyword;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = {SearchController.class, SearchPostController.class})
public class SearchExceptionHandler {

    @ExceptionHandler(SearchKeywordNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleSearchKeywordNotFound(SearchKeywordNotFound ex) {
        return errorResponse(ApiResponseCode.SEARCH_KEYWORD_NOT_FOUND);
    }

    @ExceptionHandler(InvalidSearchRequest.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSearchRequest(InvalidSearchRequest ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(InvalidSearchKeyword.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSearchKeyword(InvalidSearchKeyword ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    private static ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode code) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.fail(code, code.getDefaultMessage()));
    }
}
