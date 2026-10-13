package com.leets.blog.common.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 파라미터의 타입이 맞지 않거나(숫자 자리에 문자) 필수 헤더가 없을 때 응답할 ErrorCode를 지정함.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindErrorCode {
    ErrorCode value();
}
