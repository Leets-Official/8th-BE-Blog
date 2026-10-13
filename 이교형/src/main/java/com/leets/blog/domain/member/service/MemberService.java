package com.leets.blog.domain.member.service;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.member.entity.Member;
import com.leets.blog.domain.member.repository.MemberRepository;
import com.leets.blog.domain.member.web.dto.request.MemberSignUpRequest;
import com.leets.blog.domain.member.web.dto.response.MemberSignUpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입 (비밀번호는 현재 평문 저장)
    @Transactional
    public MemberSignUpResponse signUp(MemberSignUpRequest request) {
        // 탈퇴한 회원의 이메일도 email unique 제약 때문에 재사용 불가 → 중복으로 처리
        if (memberRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .age(request.age())
                .build();
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // 같은 이메일로 동시에 가입해 중복 확인을 함께 통과한 경우 (DB unique 제약에서 걸림)
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        return MemberSignUpResponse.from(member);
    }
}
