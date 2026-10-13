package org.example._thbe.global.exception;

import lombok.Getter;
import org.example._thbe.global.code.BaseCode;

@Getter
public class CustomException extends RuntimeException {
    BaseCode baseCode;
    public CustomException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }
}
