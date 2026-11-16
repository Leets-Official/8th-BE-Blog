package com.example.demo.dto.auth;

import com.example.demo.entity.Member;

// 비밀번호는 해시값이라도 응답에 절대 담지 않는다
public record SignupResponse(Long memberId, String email, String nickname) {

    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
