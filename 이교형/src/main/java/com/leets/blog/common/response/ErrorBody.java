package com.leets.blog.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

// example을 두면 성공 응답 예시의 error에도 표시되어 설명만 적음 (에러 예시는 @ApiErrorCodes가 ErrorCode별로 만듦)
@Schema(description = "에러 정보")
public record ErrorBody(

        @Schema(description = "에러 코드 (ErrorCode 이름)")
        String code,

        @Schema(description = "에러 메시지")
        String message
){

}
