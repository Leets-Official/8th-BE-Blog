package org.example._thbe.domain.member.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements BaseCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_404_1","존재하지 않는 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
