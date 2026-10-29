package com.leets_8th_be.kanghyeonwoo.user.domain.model;

public record UserId(Long value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }
}
