package com.leets.choyeji.dto;

import com.leets.choyeji.entity.Member;

public record SignupResponse(Long memberId, String email, String nickname) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
