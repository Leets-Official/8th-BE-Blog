package org.example._thbe.global.restdocs;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import java.lang.reflect.Method;

import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.member.enums.MemberRole;
import org.example._thbe.global.auth.CustomUserDetails;
import org.example._thbe.global.code.BaseCode;
import org.example._thbe.global.code.ErrorCode;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;


public abstract class RestDocsSupport {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtUtil jwtUtil;
    @MockitoBean
    protected UserDetailsService userDetailsService;


    private String testName;

    @BeforeEach
    void captureTestName(TestInfo testInfo) {
        this.testName = testInfo.getTestMethod().map(Method::getName).orElse("unknown");
    }

    protected static RequestPostProcessor loginAs(long id, MemberRole role) {
        Member member = Member.create("테스터", "pw", "test@test.com", role);
        ReflectionTestUtils.setField(member, "id", id);
        CustomUserDetails principal = CustomUserDetails.from(member);

        RequestPostProcessor auth = authentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-access-token");
            return auth.postProcessRequest(request);
        };
    }

    protected ResultActions expectSuccess(ResultActions result, SuccessCode code, Snippet snippet) throws Exception {
        return expectAndDocument(result, code, code.getCode(), snippet);
    }

    protected ResultActions expectError(ResultActions result, BaseCode code, ApiSpec spec) throws Exception {
        return expectAndDocument(result, code, code.getCode(), spec.error());
    }

    protected ResultActions expectValidationError(ResultActions result, String caseName, ApiSpec spec) throws Exception {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        return expectAndDocument(result, code, code.getCode() + "-" + caseName, spec.validationError());
    }

    private ResultActions expectAndDocument(ResultActions result, BaseCode code, String exampleName, Snippet snippet) throws Exception {
        return result
                .andExpect(status().is(code.getStatus().value()))
                .andDo(MockMvcRestDocumentationWrapper.document(exampleName + "__" + testName, snippet));
    }
}