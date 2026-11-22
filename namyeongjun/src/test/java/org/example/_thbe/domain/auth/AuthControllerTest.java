package org.example._thbe.domain.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

import jakarta.servlet.http.Cookie;
import org.example._thbe.domain.auth.controller.AuthController;
import org.example._thbe.domain.auth.dto.AuthLoginResponse;
import org.example._thbe.domain.auth.dto.AuthRefreshResponse;
import org.example._thbe.domain.auth.error.AuthErrorCode;
import org.example._thbe.domain.auth.service.AuthService;
import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.config.RestDocsSecurityConfig;
import org.example._thbe.global.exception.CustomException;
import org.example._thbe.global.exception.GlobalExceptionHandler;
import org.example._thbe.global.restdocs.ApiSpec;
import org.example._thbe.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AuthController.class)
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class, RestDocsSecurityConfig.class})
class AuthControllerTest extends RestDocsSupport {

    @MockitoBean AuthService authService;

    private static final String TAG = "Auth";

    private static final ApiSpec SIGNUP = ApiSpec
            .of(TAG, "회원가입", "이름, 이메일, 비밀번호로 회원가입합니다. 이미 가입된 이메일은 사용할 수 없습니다.")
            .request("MemberCreateRequest",
                    fieldWithPath("name").type(JsonFieldType.STRING).description("이름"),
                    fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"));

    private static final ApiSpec LOGIN = ApiSpec
            .of(TAG, "로그인", "이메일/비밀번호로 로그인합니다. 리프레시 토큰은 HttpOnly 쿠키(refreshToken)로 내려갑니다.")
            .request("AuthLoginRequest",
                    fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                    fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"));

    private static final ApiSpec REFRESH = ApiSpec
            .of(TAG, "액세스 토큰 재발급",
                    "refreshToken 쿠키로 새 액세스 토큰을 발급합니다. 쿠키 path는 /auth/refresh 입니다.");

    private static final String BODY_SIGNUP = """
            {"name":"홍길동","email":"a@a.com","password":"pw1234!!"}
            """;
    private static final String BODY_LOGIN = """
            {"email":"a@a.com","password":"pw1234!!"}
            """;
    private static final String BODY_LOGIN_WRONG = """
            {"email":"a@a.com","password":"wrong"}
            """;

    @Test
    void signupSuccess() throws Exception {
        expectSuccess(requestSignup(BODY_SIGNUP), SuccessCode.COMMON_CREATED, SIGNUP.success());
    }

    @Test
    void signupEmailAlreadyExist() throws Exception {
        willThrow(new CustomException(MemberErrorCode.EMAIL_ALREADY_EXIST))
                .given(authService).createMember(any());

        expectError(requestSignup(BODY_SIGNUP), MemberErrorCode.EMAIL_ALREADY_EXIST, SIGNUP);
    }

    @Test
    void loginSuccess() throws Exception {
        given(authService.loginMember(any()))
                .willReturn(new AuthLoginResponse("access.jwt", 1760000000000L, "refresh.jwt", 1761000000000L));

        expectSuccess(requestLogin(BODY_LOGIN), SuccessCode.COMMON_OK,
                LOGIN.success("AuthLoginResponse",
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("액세스 토큰"),
                        fieldWithPath("data.accessExp").type(JsonFieldType.NUMBER).description("액세스 토큰 만료 시각(epoch ms)"),
                        // 바디에서 refreshToken을 제거하면 아래 두 줄 삭제
                        fieldWithPath("data.refreshToken").type(JsonFieldType.STRING).description("리프레시 토큰"),
                        fieldWithPath("data.refreshExp").type(JsonFieldType.NUMBER).description("리프레시 토큰 만료 시각(epoch ms)")))
                .andExpect(cookie().value("refreshToken", "refresh.jwt"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/auth/refresh"));
    }

    @Test
    void loginFailed() throws Exception {
        given(authService.loginMember(any()))
                .willThrow(new CustomException(MemberErrorCode.LOGIN_FAILED));

        expectError(requestLogin(BODY_LOGIN_WRONG), MemberErrorCode.LOGIN_FAILED, LOGIN);
    }

    @Test
    void refreshSuccess() throws Exception {
        given(authService.refreshToken("refresh.jwt"))
                .willReturn(new AuthRefreshResponse("new.access.jwt"));

        expectSuccess(requestRefresh("refresh.jwt"), SuccessCode.COMMON_OK,
                REFRESH.success("AuthRefreshResponse",
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새 액세스 토큰")));
    }

    @Test
    void refreshTokenInvalid() throws Exception {
        given(authService.refreshToken("bad.jwt"))
                .willThrow(new CustomException(AuthErrorCode.TOKEN_INVALID));

        expectError(requestRefresh("bad.jwt"), AuthErrorCode.TOKEN_INVALID, REFRESH);
    }

    private ResultActions requestSignup(String body) throws Exception {
        return mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions requestLogin(String body) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions requestRefresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)));
    }
}