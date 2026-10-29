package org.example._thbe.domain.member.dto;


import org.example._thbe.domain.member.entity.Member;

public record MemberCreateResponse(Long userId,String name) {

    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(member.getId(), member.getNickname());
    }
}
