package com.leets.blog.global.swagger;

import com.leets.blog.global.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// API에서 발생할 수 있는 에러 코드를 적어두면, Swagger 문서에 실패 응답 예시로 추가된다.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {

    ErrorCode[] value();
}
