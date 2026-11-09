package com.leets_8th_be.kanghyeonwoo.shared.security;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public UserId requireId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            throw new AuthenticationCredentialsNotFoundException("로그인이 필요합니다.");
        return new UserId(Long.parseLong(auth.getName()));
    }
}
