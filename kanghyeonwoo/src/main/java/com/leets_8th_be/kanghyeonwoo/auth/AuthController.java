package com.leets_8th_be.kanghyeonwoo.auth;

import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService.Tokens;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public record Signup(
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 50) String nickname) {
        public Signup {
            if (email != null) {
                email = AuthService.email(email);
            }
            if (nickname != null) {
                nickname = nickname.trim();
            }
        }
    }

    public record Login(
            @NotBlank @Email String email,
            @NotBlank @Size(max = 72) String password) {
        public Login {
            if (email != null) {
                email = AuthService.email(email);
            }
        }
    }

    public record Refresh(@NotBlank String refreshToken) {}

    public record SignupResponse(long userId) {}

    @ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE, ApiResponseCode.DUPLICATE_RESOURCE})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody Signup request) {
        long userId = auth.signup(request.email(), request.password(), request.nickname());
        SignupResponse response = new SignupResponse(userId);
        ApiResponse<SignupResponse> body =
                ApiResponse.success(ApiResponseCode.RESOURCE_CREATED, "회원가입이 완료되었습니다.", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE, ApiResponseCode.UNAUTHORIZED})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/login")
    public ApiResponse<Tokens> login(@Valid @RequestBody Login request) {
        Tokens tokens = auth.login(request.email(), request.password());
        return ApiResponse.success(tokens);
    }

    @ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE, ApiResponseCode.UNAUTHORIZED})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/reissue")
    public ApiResponse<Tokens> reissue(@Valid @RequestBody Refresh request) {
        Tokens tokens = auth.reissue(request.refreshToken());
        return ApiResponse.success(tokens);
    }

    @ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE, ApiResponseCode.UNAUTHORIZED})
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "성공",
            useReturnTypeSchema = true)
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody Refresh request) {
        auth.logout(request.refreshToken());
        return ApiResponse.success(null);
    }
}
