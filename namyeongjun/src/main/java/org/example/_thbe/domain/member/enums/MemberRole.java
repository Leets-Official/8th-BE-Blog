package org.example._thbe.domain.member.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MemberRole {
    MEMBER("member"),ADMIN("admin");

    private final String role;
}
