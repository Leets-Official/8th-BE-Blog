package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

import com.leets_8th_be.kanghyeonwoo.auth.application.model.AuthenticationAccount;
import java.util.Optional;

public interface AccountPort {
    Optional<AuthenticationAccount> byEmail(String email);

    Optional<AuthenticationAccount> byKakaoId(String id);

    Optional<AuthenticationAccount> byId(long id);

    boolean emailExists(String email);

    AuthenticationAccount register(String email, String nickname, String passwordHash, String kakaoId);
}
