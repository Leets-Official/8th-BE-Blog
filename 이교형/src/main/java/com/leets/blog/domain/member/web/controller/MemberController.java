package com.leets.blog.domain.member.web.controller;

import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.common.response.ApiResponse;
import com.leets.blog.common.swagger.ApiErrorCodes;
import com.leets.blog.domain.member.service.MemberService;
import com.leets.blog.domain.member.web.dto.request.MemberSignUpRequest;
import com.leets.blog.domain.member.web.dto.response.MemberSignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member API", description = "회원 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원을 등록합니다. 응답의 memberId를 Authorize 버튼에 넣으면 자물쇠가 표시된 API의 X-Member-Id 헤더로 사용됩니다.")
    @ApiErrorCodes({ErrorCode.REQUIRED_FIELD_MISSING, ErrorCode.INVALID_EMAIL_FORMAT, ErrorCode.INVALID_MEMBER_INPUT,
            ErrorCode.DUPLICATE_EMAIL})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<MemberSignUpResponse>> signUp(
            @RequestBody @Valid MemberSignUpRequest request) {
        return ApiResponse.createdEntity(memberService.signUp(request));
    }
}
