package com.leets_8th_be.kanghyeonwoo.auth;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import com.leets_8th_be.kanghyeonwoo.shared.web.*;
@RestControllerAdvice(basePackages="com.leets_8th_be.kanghyeonwoo.auth")
@org.springframework.core.annotation.Order(-100)
public class AuthExceptionHandler {
 @ExceptionHandler(AuthFailure.class) ResponseEntity<?> failure(AuthFailure e) {return ResponseEntity.status(e.code.getHttpStatus()).body(ApiResponse.fail(e.code,e.getMessage()));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> duplicate(DataIntegrityViolationException e) {return ResponseEntity.status(409).body(ApiResponse.fail(ApiResponseCode.DUPLICATE_RESOURCE,"이미 가입된 이메일 또는 사용 중인 닉네임입니다. 기존 로그인 수단을 확인해 주세요."));}
 @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class) ResponseEntity<?> denied(Exception e) {return ResponseEntity.status(403).body(ApiResponse.fail(ApiResponseCode.ACCESS_DENIED,ApiResponseCode.ACCESS_DENIED.getDefaultMessage()));}
}
