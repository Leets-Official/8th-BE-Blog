package com.example.demo.service;

import com.example.demo.domain.Member;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.SignupRequest;
import com.example.demo.dto.response.SignupResponse;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.exception.BusinessException;
import com.example.demo.repository.MemberRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static com.example.demo.exception.ErrorCode.*;

@Service
public class AuthService {
    private final MemberRepository members;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final String dummyPasswordHash;

    public AuthService(MemberRepository members, PasswordEncoder passwords, TokenService tokens) {
        this.members = members;
        this.passwords = passwords;
        this.tokens = tokens;
        this.dummyPasswordHash = passwords.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String nickname = request.nickname().strip();
        if (members.existsByEmail(email) || members.existsByName(nickname)) {
            throw new BusinessException(DUPLICATE_MEMBER);
        }

        try {
            Member member = members.saveAndFlush(new Member(nickname, email, passwords.encode(request.password())));
            return new SignupResponse(member.getId(), member.getEmail(), member.getName());
        } catch (DataIntegrityViolationException error) {
            if (error.getCause() instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null) {
                String name = violation.getConstraintName().toLowerCase(Locale.ROOT);
                // MySQL can qualify the constraint name, and H2 can report the backing index.
                if (name.contains("uk_member_email") || name.contains("uk_member_nickname")) {
                    throw new BusinessException(DUPLICATE_MEMBER);
                }
            }
            throw error;
        }
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Member member = members.findByEmailAndDeletedAtIsNull(normalizeEmail(request.email())).orElse(null);
        String hash = member == null ? null : member.getPasswordHash();
        if (!passwords.matches(request.password(), hash == null ? dummyPasswordHash : hash) || hash == null) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }
        return tokens.issue(member);
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
