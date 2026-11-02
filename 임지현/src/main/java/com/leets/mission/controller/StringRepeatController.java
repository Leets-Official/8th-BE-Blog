package com.leets.mission.controller;

import com.leets.mission.dto.StringRepeatRequest;
import com.leets.mission.dto.StringRepeatResponse;
import com.leets.mission.service.StringRepeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "문자열 반복", description = "문자열 반복 API")
@RestController
@RequiredArgsConstructor
public class StringRepeatController {

    private final StringRepeatService stringRepeatService;

    @Operation(
            summary = "문자열 반복",
            description = "요청한 문자열을 반복한 결과를 반환합니다."
    )
    @PostMapping("/string/repeat")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "문자열 반복 결과 반환 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StringRepeatResponse.class)
                    )
            )
    })
    public StringRepeatResponse repeatString(
            @RequestBody StringRepeatRequest request
    ) {
        return stringRepeatService.repeatString(request);
    }
}
