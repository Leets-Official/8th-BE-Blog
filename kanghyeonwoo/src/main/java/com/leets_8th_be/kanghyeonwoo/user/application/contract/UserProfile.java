package com.leets_8th_be.kanghyeonwoo.user.application.contract;

import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;

public record UserProfile(UserId id, String username, String email) {
}
