package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserProfileStore implements UserProfileStore {
    private final UserProfileJpaRepository users;

    public JpaUserProfileStore(UserProfileJpaRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserProfile> findById(UserId id) {
        return users.findById(id.value()).map(JpaUserProfileStore::toProfile);
    }

    @Override
    public Optional<UserProfile> findByEmail(String email) {
        return users.findByEmail(email).map(JpaUserProfileStore::toProfile);
    }

    @Override
    public UserProfile create(String username, String email) {
        UserProfileJpaEntity saved = users.saveAndFlush(new UserProfileJpaEntity(username, email));
        return toProfile(saved);
    }

    private static UserProfile toProfile(UserProfileJpaEntity user) {
        return new UserProfile(new UserId(user.getId()), user.getUsername(), user.getEmail());
    }
}
