package com.leets_8th_be.kanghyeonwoo.user.application.usecase;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileLookup;
import com.leets_8th_be.kanghyeonwoo.user.application.port.out.UserProfileStore;

import java.util.Optional;

public class FindUserProfileService implements UserProfileLookup {
    private final UserProfileStore profiles;

    public FindUserProfileService(UserProfileStore profiles) {
        this.profiles = profiles;
    }

    @Override
    public Optional<UserProfile> findById(UserId id) {
        return profiles.findById(id);
    }
}
