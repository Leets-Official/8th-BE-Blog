package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.domain.SocialAccount;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

import static com.example.demo.domain.SocialAccount.Provider.KAKAO;
import static com.example.demo.exception.ErrorCode.KAKAO_LOGIN_FAILED;

@Service
@RequiredArgsConstructor
public class KakaoLoginService {
    private final SocialAccountRepository accounts;
    private final MemberRepository members;
    private final TokenService tokens;
    private final TransactionTemplate transaction;

    public TokenResponse login(long kakaoUserId) {
        String providerUserId = Long.toString(kakaoUserId);
        try {
            return transaction.execute(status -> {
                Member member = findAccount(providerUserId)
                        .map(SocialAccount::getMember)
                        .orElseGet(() -> register(providerUserId));
                return issueForActiveMember(member);
            });
        } catch (DataIntegrityViolationException conflict) {
            // A concurrent first login may have committed the same Kakao identity.
            return transaction.execute(status -> issueForActiveMember(findAccount(providerUserId)
                    .orElseThrow(() -> conflict)
                    .getMember()));
        }
    }

    private Optional<SocialAccount> findAccount(String providerUserId) {
        return accounts.findByProviderAndProviderUserId(KAKAO, providerUserId);
    }

    private Member register(String providerUserId) {
        Member member = members.save(new Member("kakao-" + UUID.randomUUID(), null, null));
        accounts.saveAndFlush(new SocialAccount(KAKAO, providerUserId, member));
        return member;
    }

    private TokenResponse issueForActiveMember(Member member) {
        if (member.isDeleted()) {
            throw new BusinessException(KAKAO_LOGIN_FAILED);
        }
        return tokens.issue(member);
    }
}
