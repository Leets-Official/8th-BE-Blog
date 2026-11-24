package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.global.ApiResponse;
import com.example.demo.global.ErrorResponse;
import com.example.demo.global.SuccessCode;
import com.example.demo.global.SwaggerErrorExamples;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 참고: Swagger 쪽 ApiResponse 는 이름이 겹쳐서 패키지명을 모두 적어 사용한다. (PostController 와 동일)
@Tag(name = "Auth", description = "회원가입 / 로그인 / 토큰 재발급 API (인증 없이 호출 가능)")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // @ResponseStatus 는 문서용 (실제 상태 코드는 ApiResponse.success 가 결정)
    @Operation(summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 가입합니다. 이메일은 대소문자를 구분하지 않고(소문자로 변환해) 비교하며, 이메일과 닉네임은 중복될 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 비밀번호 72바이트 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "COMMON_400_1", summary = "입력값 검증 실패", value = SwaggerErrorExamples.INVALID_SIGNUP),
                                    @ExampleObject(name = "AUTH_400_1", summary = "비밀번호 72바이트 초과", value = SwaggerErrorExamples.PASSWORD_TOO_LONG)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "USER_409_1", summary = "이메일 중복", value = SwaggerErrorExamples.DUPLICATE_EMAIL),
                                    @ExampleObject(name = "USER_409_2", summary = "닉네임 중복", value = SwaggerErrorExamples.DUPLICATE_NICKNAME)
                            }))
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(SuccessCode.SIGNUP_OK, authService.signup(request));
    }

    @Operation(summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 Access Token, Refresh Token 을 발급받습니다. "
                    + "이메일이 없는 경우와 비밀번호가 틀린 경우는 같은 401 로 응답합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_LOGIN))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "AUTH_401_1", value = SwaggerErrorExamples.LOGIN_FAILED)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(SuccessCode.LOGIN_OK, authService.login(request));
    }

    @Operation(summary = "토큰 재발급",
            description = "Refresh Token 으로 Access Token 과 새 Refresh Token 을 발급합니다. "
                    + "재발급할 때마다 Refresh Token 이 교체되며, 이미 사용한 Refresh Token 을 다시 보내면 401 이고 재발급 권한이 폐기되어 다시 로그인해야 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "COMMON_400_1", value = SwaggerErrorExamples.INVALID_REISSUE))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 이미 사용한 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "AUTH_401_3", value = SwaggerErrorExamples.INVALID_REFRESH_TOKEN)))
    })
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest request) {
        return ApiResponse.success(SuccessCode.TOKEN_REISSUED, authService.reissue(request.refreshToken()));
    }
}
