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
}
