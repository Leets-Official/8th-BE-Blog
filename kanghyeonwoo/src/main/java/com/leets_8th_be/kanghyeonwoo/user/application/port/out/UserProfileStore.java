package com.leets_8th_be.kanghyeonwoo.user.application.port.out;

import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

import java.util.Optional;

public interface UserProfileStore {
    Optional<UserProfile> findById(UserId id);
    Optional<UserProfile> findByEmail(String email);
    UserProfile create(String username, String email);
}
