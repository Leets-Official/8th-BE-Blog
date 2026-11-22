package org.example._thbe.global.auth;

import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.auth.error.AuthErrorCode;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.member.repository.MemberRepository;
import org.example._thbe.global.exception.CustomException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        Member member = memberRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(AuthErrorCode.TOKEN_INVALID));
        return CustomUserDetails.from(member);
    }
}
