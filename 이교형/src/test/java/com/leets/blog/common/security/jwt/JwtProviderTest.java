package com.leets.blog.common.security.jwt;

import com.leets.blog.common.exception.CustomException;
import com.leets.blog.common.exception.ErrorCode;
import com.leets.blog.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 스프링 없이 JwtProvider만 검증 (키는 테스트 전용 더미 값)
class JwtProviderTest {

    private static final String SECRET = "QzLmJ0VdZMQzQgqizlQz/Wo82Tu5paLC7OF2YkCkQDw2sRdxmYfpOBC7cRVhVMV0";

    private final JwtProvider jwtProvider = provider(Duration.ofMinutes(30), Duration.ofDays(14));

    private static JwtProvider provider(Duration access, Duration refresh) {
        return new JwtProvider(new JwtProperties(SECRET, access, refresh));
    }

    private static ErrorCode errorCodeOf(Runnable action) {
        try {
            action.run();
        } catch (CustomException e) {
            return e.getErrorCode();
        }
        throw new AssertionError("CustomException이 발생하지 않음");
    }

    @Test
    void Access_Token_발급_후_검증하면_회원ID와_권한이_나온다() {
        String token = jwtProvider.createAccessToken(7L, MemberRole.ADMIN);

        JwtProvider.AccessTokenClaims claims = jwtProvider.parseAccessToken(token);

        assertThat(claims.memberId()).isEqualTo(7L);
        assertThat(claims.role()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 만료된_Access_Token은_EXPIRED_TOKEN() {
        String expired = provider(Duration.ofSeconds(-1), Duration.ofDays(14)).createAccessToken(1L, MemberRole.MEMBER);

        assertThat(errorCodeOf(() -> jwtProvider.parseAccessToken(expired))).isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    void 위조_형식오류_Refresh_Token은_Access_검증에서_INVALID_TOKEN() {
        String token = jwtProvider.createAccessToken(1L, MemberRole.MEMBER);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");
        String otherKeyToken = new JwtProvider(new JwtProperties(
                "b3RoZXItc2VjcmV0LWtleS1mb3ItdGVzdC1vbmx5LTEyMzQ1Njc4OTA=", Duration.ofMinutes(30), Duration.ofDays(14)))
                .createAccessToken(1L, MemberRole.ADMIN);

        assertThat(errorCodeOf(() -> jwtProvider.parseAccessToken(tampered))).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(errorCodeOf(() -> jwtProvider.parseAccessToken(otherKeyToken))).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(errorCodeOf(() -> jwtProvider.parseAccessToken("abc.def.ghi"))).isEqualTo(ErrorCode.INVALID_TOKEN);
        assertThat(errorCodeOf(() -> jwtProvider.parseAccessToken(jwtProvider.createRefreshToken(1L))))
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void Refresh_Token_검증() {
        String refresh = jwtProvider.createRefreshToken(3L);
        String expired = provider(Duration.ofMinutes(30), Duration.ofSeconds(-1)).createRefreshToken(3L);

        assertThat(jwtProvider.parseRefreshToken(refresh)).isEqualTo(3L);
        assertThat(errorCodeOf(() -> jwtProvider.parseRefreshToken(expired))).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
        assertThat(errorCodeOf(() -> jwtProvider.parseRefreshToken(jwtProvider.createAccessToken(3L, MemberRole.MEMBER))))
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 같은_순간에_발급한_Refresh_Token도_매번_다르다() {
        assertThat(jwtProvider.createRefreshToken(1L)).isNotEqualTo(jwtProvider.createRefreshToken(1L));
    }

    @Test
    void 키가_32바이트보다_짧으면_생성_실패() {
        assertThatThrownBy(() -> new JwtProvider(new JwtProperties("c2hvcnQta2V5", Duration.ofMinutes(30), Duration.ofDays(14))))
                .isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
    }
}
