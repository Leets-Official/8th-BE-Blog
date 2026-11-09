package com.leets_8th_be.kanghyeonwoo.auth;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
@RestController
@RequestMapping("/api/v2/auth/kakao")
public class KakaoController {
 private final KakaoAuthService kakao;
 public KakaoController(KakaoAuthService k) {kakao=k;}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.OAUTH_UNAVAILABLE})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="302",description="성공",useReturnTypeSchema=true)
 @GetMapping("/start") public ResponseEntity<Void> start(jakarta.servlet.http.HttpServletRequest req) {
  var s=kakao.start();var cookie=ResponseCookie.from("kakao_browser",s.browser()).httpOnly(true).secure(req.isSecure()).sameSite("Lax").path("/api/v2/auth/kakao").maxAge(600).build();
  return ResponseEntity.status(302).header(HttpHeaders.SET_COOKIE,cookie.toString()).location(java.net.URI.create(s.url())).build();
 }
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.OAUTH_UNAVAILABLE})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="302",description="성공",useReturnTypeSchema=true)
 @GetMapping("/callback") public ResponseEntity<Void> callback(@RequestParam String state,@RequestParam String code,@CookieValue(name="kakao_browser",required=false) String browser) {
  return ResponseEntity.status(302).location(java.net.URI.create(kakao.callback(state,browser,code))).build();
 }
 public record Code(@NotBlank String code) {}
 public record Signup(@NotBlank String signupToken,@NotBlank @Size(max=50) String nickname) { public Signup { if(nickname!=null) nickname=nickname.trim(); } }
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.OAUTH_UNAVAILABLE})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
 @PostMapping("/exchange") public ApiResponse<?> exchange(@Valid @RequestBody Code r,@CookieValue(name="kakao_browser",required=false) String browser) {return ApiResponse.success(kakao.exchange(r.code(),browser));}
 @com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes({com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.UNAUTHORIZED,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.INVALID_INPUT_VALUE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.DUPLICATE_RESOURCE,com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode.OAUTH_UNAVAILABLE})
 @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200",description="성공",useReturnTypeSchema=true)
 @PostMapping("/signup") public ApiResponse<?> signup(@Valid @RequestBody Signup r,@CookieValue(name="kakao_browser",required=false) String browser) {return ApiResponse.success(kakao.signup(r.signupToken(),browser,r.nickname()));}
}
