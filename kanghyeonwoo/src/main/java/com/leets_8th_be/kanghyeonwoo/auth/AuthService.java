package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService.Tokens;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthenticationService service;

    public AuthService(AuthenticationService service) {
        this.service = service;
    }

    public static String email(String raw) {
        return AuthenticationService.email(raw);
    }

    static String hash(String raw) {
        return AuthenticationService.hash(raw);
    }

    @Transactional
    public long signup(String email, String password, String nickname) {
        return service.signup(email, password, nickname);
    }

    @Transactional
    public Tokens login(String email, String password) {
        return service.login(email, password);
    }

    @Transactional
    public Tokens start(long userId) {
        return service.start(userId);
    }

    @Transactional(noRollbackFor = AuthFailure.class)
    public Tokens reissue(String token) {
        return service.reissue(token);
    }

    @Transactional
    public void logout(String token) {
        service.logout(token);
    }
}
