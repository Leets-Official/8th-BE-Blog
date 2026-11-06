package com.leets.blog.controller;

import com.leets.blog.dto.RepeatStringRequest;
import com.leets.blog.dto.RepeatStringResponse;
import com.leets.blog.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "기본", description = "서버 상태 확인 및 문자열 반환 API")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "헬스체크", description = "서버가 정상 동작 중이면 문자열 ok를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상 동작",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                    examples = @ExampleObject(value = "ok")))
    public String health() {
        return assignmentService.health();
    }

    @PostMapping(value = "/string/repeat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "문자열 두 개 반환", description = "입력한 value를 string_one과 string_two에 각각 담아 반환합니다.")
    @ApiResponse(responseCode = "200", description = "동일한 문자열을 두 필드에 반환")
    public RepeatStringResponse repeat(@RequestBody RepeatStringRequest request) {
        return assignmentService.repeat(request.value());
    }
}
