package com.leets.mission.global.apiPayload.exception;

import com.leets.mission.global.apiPayload.code.ErrorStatus;
import lombok.Getter;

// 서비스 로직에서 발생하는 예외 (어떤 에러인지 ErrorStatus로 전달)
@Getter
public class GeneralException extends RuntimeException {

    private final ErrorStatus errorStatus;

    public GeneralException(ErrorStatus errorStatus) {
        super(errorStatus.getMessage());
        this.errorStatus = errorStatus;
    }
}
