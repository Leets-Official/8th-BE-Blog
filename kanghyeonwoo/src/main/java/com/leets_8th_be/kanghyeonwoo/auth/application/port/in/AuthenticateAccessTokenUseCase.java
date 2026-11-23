package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticatedUser;

public interface AuthenticateAccessTokenUseCase {
    AuthenticatedUser authenticate(String accessToken);
}
