package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;

public interface SignupWithKakaoUseCase {
    Tokens signup(String signupToken, String browser, String nickname);
}
