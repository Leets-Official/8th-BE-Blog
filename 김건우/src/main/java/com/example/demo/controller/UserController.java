package com.example.demo.controller;

import com.example.demo.dto.UserResponse;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "Access Token 의 사용자 정보를 조회합니다. 사용자는 요청 값이 아니라 검증된 토큰에서 결정됩니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return ApiResponse.success(SuccessCode.USER_ME_OK, userService.getMe(userId));
    }
}
