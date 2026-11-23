package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;

public interface LoginUseCase {
    Tokens login(String email, String password);
}
