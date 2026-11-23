package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

import com.leets_8th_be.kanghyeonwoo.auth.domain.model.AuthenticationSession;
import java.util.Optional;

public interface LoginSessionStore {
    Optional<AuthenticationSession> findForUpdate(String id);

    void save(AuthenticationSession session);
}
