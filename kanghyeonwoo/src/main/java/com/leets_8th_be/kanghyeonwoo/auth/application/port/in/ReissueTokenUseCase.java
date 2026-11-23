package com.leets_8th_be.kanghyeonwoo.auth.application.port.in;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.Tokens;

public interface ReissueTokenUseCase {
    Tokens reissue(String refreshToken);
}
