package org.example._thbe.domain.member.service;



import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.member.dto.MemberCreateRequest;
import org.example._thbe.domain.member.dto.MemberCreateResponse;
import org.example._thbe.domain.member.entity.Member;
import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.domain.member.repository.MemberRepository;
import org.example._thbe.global.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;


    //테스트용 로직
    @Transactional
    public MemberCreateResponse createUser(MemberCreateRequest request) {

        Member member = Member.create(
                request.name(),
                request.password(),
                request.email());

        memberRepository.save(member);
        return MemberCreateResponse.from(member);
    }

    public Member getMember(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member;
    }

    public String getUserName(Long userId) {
        Member member = memberRepository.findById(userId).orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member.getNickname();
    }
}
