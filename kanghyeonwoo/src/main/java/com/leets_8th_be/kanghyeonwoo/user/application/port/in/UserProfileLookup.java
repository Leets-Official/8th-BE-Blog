package com.leets_8th_be.kanghyeonwoo.user.application.port.in;

import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

import java.util.Optional;

public interface UserProfileLookup {
    Optional<UserProfile> findById(UserId id);
}
