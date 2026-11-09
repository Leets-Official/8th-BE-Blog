package com.leets_8th_be.kanghyeonwoo.blog.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.blog.application.exception.AuthorNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.CommentNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.InvalidCommentQuery;
import com.leets_8th_be.kanghyeonwoo.blog.application.exception.PostNotFound;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostAccessDenied;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = {PostController.class, CommentController.class})
public class BlogExceptionHandler {

    @ExceptionHandler(AuthorNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorNotFound(AuthorNotFound ex) {
        return errorResponse(ApiResponseCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(PostNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handlePostNotFound(PostNotFound ex) {
        return errorResponse(ApiResponseCode.POST_NOT_FOUND);
    }

    @ExceptionHandler(PostAccessDenied.class)
    public ResponseEntity<ApiResponse<Void>> handlePostAccessDenied(PostAccessDenied ex) {
        return errorResponse(ApiResponseCode.POST_ACCESS_DENIED);
    }

    @ExceptionHandler(InvalidCommentQuery.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCommentQuery(InvalidCommentQuery ex) {
        return errorResponse(ApiResponseCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(CommentNotFound.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFound(CommentNotFound ex) {
        return errorResponse(ApiResponseCode.COMMENT_NOT_FOUND);
    }

    private static ResponseEntity<ApiResponse<Void>> errorResponse(ApiResponseCode code) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(code, code.getDefaultMessage()));
    }
}
