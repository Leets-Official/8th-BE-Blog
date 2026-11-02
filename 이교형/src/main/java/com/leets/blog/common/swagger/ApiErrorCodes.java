package com.leets.blog.common.swagger;

import com.leets.blog.common.exception.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 이 API가 응답할 수 있는 에러를 적으면 Swagger 응답 목록에 상태 코드별 예시로 표시됨
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {
    ErrorCode[] value();
}
