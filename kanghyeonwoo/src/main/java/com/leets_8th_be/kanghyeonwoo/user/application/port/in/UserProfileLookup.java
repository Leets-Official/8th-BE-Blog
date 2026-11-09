package com.leets_8th_be.kanghyeonwoo.user.application.port.in;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;

import java.util.Optional;

public interface UserProfileLookup {
    Optional<UserProfile> findById(UserId id);
}
