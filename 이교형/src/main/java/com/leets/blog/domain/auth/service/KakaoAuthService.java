package com.leets.blog.domain.auth.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.auth.kakao.KakaoOAuthClient;
import com.leets.blog.domain.auth.kakao.KakaoOAuthState;
import com.leets.blog.domain.auth.kakao.dto.KakaoUserResponse;
import com.leets.blog.domain.auth.web.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

// 카카오 로그인 흐름 담당. 트랜잭션 없음: 카카오 응답을 기다리는 동안 DB 커넥션을 잡지 않음
// DB 작업(회원 조회·등록, 토큰 저장)은 KakaoMemberService에서 트랜잭션으로 처리
// (같은 클래스 안의 @Transactional 메서드를 직접 부르면 트랜잭션이 걸리지 않아 빈을 나눔)
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthState kakaoOAuthState;
    private final KakaoMemberService kakaoMemberService;

    public String getAuthorizeUrl(String state) {
        return kakaoOAuthClient.getAuthorizeUrl(state);
    }

    // state 검증 → 인가 코드 → 카카오 토큰 → 카카오 사용자 정보 → 회원 조회/등록 → 서비스 토큰 발급
    public TokenResponse login(String code, String state, String savedState) {
        // 인가 코드를 교환하기 전에 이 브라우저에서 시작한 로그인인지 먼저 확인
        kakaoOAuthState.verify(savedState, state);
        // 사용자가 동의 화면에서 취소하면 code 없이 error 파라미터만 옴
        if (!StringUtils.hasText(code)) {
            throw new CustomException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
        String kakaoAccessToken = kakaoOAuthClient.getAccessToken(code);
        KakaoUserResponse kakaoUser = kakaoOAuthClient.getUserInfo(kakaoAccessToken);
        return kakaoMemberService.login(kakaoUser);
    }
}
