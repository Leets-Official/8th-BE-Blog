package com.leets_8th_be.kanghyeonwoo.user.application.port.in;

import com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials;

import java.util.Optional;

public interface AccountDirectory {
    Optional<Credentials> byEmail(String email);

    Optional<Credentials> byKakaoId(String kakaoId);

    Credentials byId(long id);

    Credentials register(String email, String nickname, String passwordHash, String kakaoId);

    boolean emailExists(String email);
}
