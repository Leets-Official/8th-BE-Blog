package com.leets.blog.controller;

import com.leets.blog.dto.StringRepeatRequest;
import com.leets.blog.dto.StringRepeatResponse;
import com.leets.blog.service.StringService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/string")
@RequiredArgsConstructor
public class StringController {

    private final StringService stringService;

    @Operation(summary = "문자열 두 번 반환", description = "입력한 문자열을 string_one과 string_two에 각각 반환합니다.")
    @PostMapping(value = "/repeat", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public StringRepeatResponse repeat(@RequestBody StringRepeatRequest request) {
        return stringService.repeat(request.value());
    }
}
