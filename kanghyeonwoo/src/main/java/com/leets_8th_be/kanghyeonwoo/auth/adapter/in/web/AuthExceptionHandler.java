package com.leets_8th_be.kanghyeonwoo.auth.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.auth.domain.exception.AuthenticationFailure;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {AuthController.class, KakaoController.class})
@Order(-100)
public class AuthExceptionHandler {
    @ExceptionHandler(AuthenticationFailure.class)
    ResponseEntity<ApiResponse<Void>> domainFailure(AuthenticationFailure failure) {
        ApiResponseCode code = switch (failure.reason()) {
            case INVALID_INPUT -> ApiResponseCode.INVALID_INPUT_VALUE;
            case INVALID_CREDENTIALS -> ApiResponseCode.UNAUTHORIZED;
            case ACCOUNT_CONFLICT -> ApiResponseCode.DUPLICATE_RESOURCE;
            case PROVIDER_UNAVAILABLE -> ApiResponseCode.OAUTH_UNAVAILABLE;
        };
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(code, failure.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> duplicate(DataIntegrityViolationException e) {
        return ResponseEntity.status(409)
                .body(ApiResponse.fail(
                        ApiResponseCode.DUPLICATE_RESOURCE, "이미 가입된 이메일 또는 사용 중인 닉네임입니다. 기존 로그인 수단을 확인해 주세요."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> denied(Exception e) {
        return ResponseEntity.status(403)
                .body(ApiResponse.fail(
                        ApiResponseCode.ACCESS_DENIED, ApiResponseCode.ACCESS_DENIED.getDefaultMessage()));
    }
}
