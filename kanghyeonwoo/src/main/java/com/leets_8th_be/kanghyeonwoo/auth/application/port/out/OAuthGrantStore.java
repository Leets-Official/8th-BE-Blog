package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

import com.leets_8th_be.kanghyeonwoo.auth.domain.model.OAuthGrant;
import java.util.Optional;

public interface OAuthGrantStore {
    Optional<OAuthGrant> findForUpdate(String hash);

    void save(OAuthGrant grant);

    void delete(OAuthGrant grant);
}
