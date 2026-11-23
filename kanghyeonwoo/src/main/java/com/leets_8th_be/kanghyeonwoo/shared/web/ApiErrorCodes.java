package com.leets_8th_be.kanghyeonwoo.shared.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 해당 API에서 발생 가능한 오류 코드. 서버 내부 오류는 문서 설정에서 공통으로 추가한다. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {
    ApiResponseCode[] value() default {};
}
