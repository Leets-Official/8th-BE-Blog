package com.leets_8th_be.kanghyeonwoo.user.application.model;

import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;

public record UserProfile(UserId id, String username, String email) {
}
