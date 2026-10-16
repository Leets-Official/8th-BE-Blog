package com.example.demo.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum GeneralSuccessCode implements BaseSuccessCode{

    SUCCESS_CODE(HttpStatus.OK,
            "COMMON200",
            "요청이 성공했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
