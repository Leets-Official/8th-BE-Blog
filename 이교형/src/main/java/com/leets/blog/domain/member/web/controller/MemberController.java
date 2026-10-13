package com.leets.blog.domain.member.web.controller;

import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.domain.member.service.MemberService;
import com.leets.blog.domain.member.web.dto.request.MemberSignUpRequest;
import com.leets.blog.domain.member.web.dto.response.MemberSignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원을 등록합니다. 응답의 memberId를 게시글 API의 X-Member-Id 헤더에 사용합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MemberSignUpResponse>> signUp(
            @RequestBody @Valid MemberSignUpRequest request) {
        return ApiResponse.createdEntity(memberService.signUp(request));
    }
}
