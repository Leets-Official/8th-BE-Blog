package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.OAuthGrantStore;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthGrant;
import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthPurpose;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaOAuthGrantStore implements OAuthGrantStore {
    private final OAuthGrantJpaRepository repository;

    public JpaOAuthGrantStore(OAuthGrantJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OAuthGrant> findForUpdate(String hash) {
        return repository.lock(hash).map(JpaOAuthGrantStore::toGrant);
    }

    @Override
    public void save(OAuthGrant grant) {
        var row = repository
                .findById(grant.hash())
                .orElseGet(() -> new OAuthGrantJpaEntity(
                        grant.hash(), grant.purpose().value(), grant.browserHash(), grant.expiresAt()));
        row.kakaoId = grant.kakaoId();
        row.email = grant.email();
        row.userId = grant.userId();
        repository.save(row);
    }

    @Override
    public void delete(OAuthGrant grant) {
        repository.deleteById(grant.hash());
        repository.flush();
    }

    private static OAuthGrant toGrant(OAuthGrantJpaEntity row) {
        return new OAuthGrant(
                row.hash,
                OAuthPurpose.fromValue(row.kind),
                row.browserHash,
                row.expiresAt,
                row.kakaoId,
                row.email,
                row.userId);
    }
}
