package com.leets_8th_be.kanghyeonwoo.auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.leets_8th_be.kanghyeonwoo.shared.web.*;
@RestController
@RequestMapping("/api/v2/auth")
public class AuthController {
 private final AuthService auth;
 public AuthController(AuthService auth) {this.auth=auth;}
 public record Signup(@NotBlank @Email @Size(max=100) String email,@NotBlank @Size(min=8,max=72) String password,@NotBlank @Size(max=50) String nickname) { public Signup { if(email!=null) email=AuthService.email(email); if(nickname!=null) nickname=nickname.trim(); } }
 public record Login(@NotBlank @Email String email,@NotBlank @Size(max=72) String password) { public Login { if(email!=null) email=AuthService.email(email); } }
 public record Refresh(@NotBlank String refreshToken) {}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE,ApiResponseCode.DUPLICATE_RESOURCE})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201",description="성공",useReturnTypeSchema=true)
 @PostMapping("/signup") public ResponseEntity<?> signup(@Valid @RequestBody Signup r) {return ResponseEntity.status(201).body(ApiResponse.success(ApiResponseCode.RESOURCE_CREATED,"회원가입이 완료되었습니다.",java.util.Map.of("userId",auth.signup(r.email(),r.password(),r.nickname()))));}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE,ApiResponseCode.UNAUTHORIZED})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
 @PostMapping("/login") public ApiResponse<?> login(@Valid @RequestBody Login r) {return ApiResponse.success(auth.login(r.email(),r.password()));}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE,ApiResponseCode.UNAUTHORIZED})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
 @PostMapping("/reissue") public ApiResponse<?> reissue(@Valid @RequestBody Refresh r) {return ApiResponse.success(auth.reissue(r.refreshToken()));}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({ApiResponseCode.INVALID_INPUT_VALUE,ApiResponseCode.UNAUTHORIZED})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
 @PostMapping("/logout") public ApiResponse<?> logout(@Valid @RequestBody Refresh r) {auth.logout(r.refreshToken());return ApiResponse.success(null);}
}
