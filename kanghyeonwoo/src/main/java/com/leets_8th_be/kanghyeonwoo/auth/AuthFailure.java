package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;

public class AuthFailure extends RuntimeException {
    final ApiResponseCode code;

    public AuthFailure(ApiResponseCode code) {
        super(
                code == ApiResponseCode.DUPLICATE_RESOURCE
                        ? "이미 가입된 계정이 있습니다. 기존 로그인 수단을 이용해 주세요."
                        : code.getDefaultMessage());
        this.code = code;
    }
}
