package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.LoginSessionStore;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.AuthenticationSession;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaLoginSessionStore implements LoginSessionStore {
    private final LoginSessionJpaRepository repository;

    public JpaLoginSessionStore(LoginSessionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AuthenticationSession> findForUpdate(String id) {
        return repository.lock(id).map(JpaLoginSessionStore::toSession);
    }

    @Override
    public void save(AuthenticationSession session) {
        var row = repository
                .findById(session.id())
                .orElseGet(() -> new LoginSessionJpaEntity(session.id(), session.userId()));
        row.refreshHash = session.refreshHash();
        row.expiresAt = session.expiresAt();
        row.revoked = session.revoked();
        repository.save(row);
    }

    private static AuthenticationSession toSession(LoginSessionJpaEntity row) {
        return new AuthenticationSession(row.id, row.userId, row.refreshHash, row.expiresAt, row.revoked);
    }
}
