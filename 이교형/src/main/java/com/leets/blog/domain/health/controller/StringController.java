package com.leets.blog.domain.health.controller;

import com.leets.blog.domain.health.dto.StringRequest;
import com.leets.blog.domain.health.dto.StringResponse;
import com.leets.blog.domain.health.service.StringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "String Repeat API", description = "문자열 2개 반환 API")
@RestController
@RequiredArgsConstructor
public class StringController {
    private final StringService stringService;
    @Operation(summary = "문자열 2개 반환 API", description = "입력한 문자열을 2개 반환합니다.")
    @PostMapping("/string/repeat")
    public StringResponse repeatString(@RequestBody StringRequest request) {

        return stringService.repeat(request.getValue());

    }
}
