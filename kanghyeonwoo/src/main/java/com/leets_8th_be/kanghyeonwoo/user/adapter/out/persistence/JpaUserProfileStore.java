package com.leets_8th_be.kanghyeonwoo.user.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaUserProfileStore implements UserProfileStore {
    private final UserProfileJpaRepository users;

    public JpaUserProfileStore(UserProfileJpaRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserProfile> findById(UserId id) {
        return users.findById(id.value())
                .map(user -> new UserProfile(new UserId(user.getId()), user.getUsername(), user.getEmail()));
    }

    @Override
    public Optional<UserProfile> findByEmail(String email) {
        return users.findByEmail(email).map(user -> new UserProfile(new UserId(user.getId()),
                user.getUsername(), user.getEmail()));
    }

    @Override
    public UserProfile create(String username, String email) {
        UserProfileJpaEntity saved = users.saveAndFlush(new UserProfileJpaEntity(username, email));
        return new UserProfile(new UserId(saved.getId()), saved.getUsername(), saved.getEmail());
    }
}
