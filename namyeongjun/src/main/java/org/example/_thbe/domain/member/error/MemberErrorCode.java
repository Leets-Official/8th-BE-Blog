package org.example._thbe.domain.member.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example._thbe.global.code.BaseCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements BaseCode {

    LOGIN_FAILED(HttpStatus.BAD_REQUEST,"MEMBER_400_1","아이디 또는 비밀번호가 일치하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_404_1","존재하지 않는 사용자입니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.CONFLICT,"MEMBER_409_1","이미 사용 중인 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
