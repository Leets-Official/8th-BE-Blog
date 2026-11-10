package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

public interface SignupUseCase {
    long signup(String email, String password, String nickname);
}
