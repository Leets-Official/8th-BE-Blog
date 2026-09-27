package org.example._thbe.domain.StringRepeat.controller;

import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.StringRepeat.dto.StringRepeatRequest;
import org.example._thbe.domain.StringRepeat.dto.StringRepeatResponse;
import org.example._thbe.domain.StringRepeat.service.StringRepeatService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/repeat")
@RestController
@RequiredArgsConstructor
public class StringRepeatController {
    private final StringRepeatService stringRepeatService;

    @PostMapping("/string")
    public ResponseEntity<ApiResponse<StringRepeatResponse>> repeatString(@RequestBody StringRepeatRequest request) {
        return ApiResponse.success(SuccessCode.COMMON_OK,stringRepeatService.repeatString(request));
    }
}
