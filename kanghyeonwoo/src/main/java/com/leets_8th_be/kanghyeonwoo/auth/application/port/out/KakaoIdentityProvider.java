package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.KakaoIdentity;

public interface KakaoIdentityProvider {
    KakaoIdentity identity(String authorizationCode);
}
