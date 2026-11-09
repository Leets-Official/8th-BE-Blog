package com.example.demo.controller;

import com.example.demo.dto.member.MemberResponse;
import com.example.demo.global.response.ApiResponse;
import com.example.demo.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "Access Token으로 로그인한 회원의 정보를 조회합니다. 토큰이 없거나 잘못되면 401(AUTH4010)을 반환합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.ok("내 정보 조회가 완료되었습니다.", memberService.getMyInfo(memberId));
    }
}