package com.leets.blog.domain.auth.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.auth.kakao.dto.KakaoUserResponse;
import com.leets.blog.domain.auth.web.dto.response.TokenResponse;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.entity.SocialAccount;
import com.leets.blog.domain.member.entity.SocialProvider;
import com.leets.blog.domain.member.repository.MemberRepository;
import com.leets.blog.domain.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

// 카카오 사용자 정보로 회원 조회·등록 후 서비스 토큰 발급 (DB 작업만 트랜잭션으로 묶음)
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoMemberService {

    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 20;
    private static final String DEFAULT_NICKNAME = "카카오회원";

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final AuthService authService;

    // 이미 연결된 카카오 계정이면 그 회원, 처음이면 새 회원으로 등록
    @Transactional
    public TokenResponse login(KakaoUserResponse kakaoUser) {
        String kakaoId = String.valueOf(kakaoUser.id());
        Member member = socialAccountRepository.findByProviderAndProviderId(SocialProvider.KAKAO, kakaoId)
                .map(SocialAccount::getMember)
                .orElseGet(() -> register(kakaoUser, kakaoId));
        return authService.issueTokens(member);
    }

    // 최초 카카오 로그인: 같은 이메일의 회원이 있어도 연결하지 않고 항상 새 회원
    // (일반 가입은 이메일 인증이 없어서, 남의 이메일로 먼저 가입해 둔 계정에 붙을 수 있음)
    private Member register(KakaoUserResponse kakaoUser, String kakaoId) {
        Member member = memberRepository.save(Member.createSocial(
                availableEmail(kakaoUser.verifiedEmail()),
                uniqueNickname(kakaoUser.nickname())));
        socialAccountRepository.save(SocialAccount.of(member, SocialProvider.KAKAO, kakaoId));
        return member;
    }

    // 이미 다른 회원(탈퇴 회원 포함)이 쓰는 이메일이면 unique 제약에 걸리므로 이메일 없이 가입
    private String availableEmail(String verifiedEmail) {
        return verifiedEmail != null && !memberRepository.existsByEmail(verifiedEmail) ? verifiedEmail : null;
    }

    // 카카오 닉네임이 이미 있으면 "닉네임_1234"처럼 숫자를 붙여 중복을 피함
    private String uniqueNickname(String kakaoNickname) {
        String base = StringUtils.hasText(kakaoNickname) && kakaoNickname.strip().length() >= NICKNAME_MIN_LENGTH
                ? cut(kakaoNickname.strip(), NICKNAME_MAX_LENGTH)
                : DEFAULT_NICKNAME;
        if (!memberRepository.existsByNickname(base)) {
            return base;
        }

        String prefix = cut(base, NICKNAME_MAX_LENGTH - 5);   // "_" + 4자리 숫자 자리
        for (int i = 0; i < 10; i++) {
            String candidate = prefix + "_" + ThreadLocalRandom.current().nextInt(1000, 10000);
            if (!memberRepository.existsByNickname(candidate)) {
                return candidate;
            }
        }
        throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    private String cut(String value, int maxLength) {
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
