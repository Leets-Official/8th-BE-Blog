package com.leets_8th_be.kanghyeonwoo.user.application.port.in;

import com.leets_8th_be.kanghyeonwoo.user.application.contract.UserProfile;

public interface CreateUserProfileUseCase {
    UserProfile create(String username, String email);
}
