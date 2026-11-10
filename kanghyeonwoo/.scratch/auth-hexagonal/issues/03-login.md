# 03: 로그인 및 JWT 인증 이전

**What to build:** 로그인 입력 포트, 세션 발급, JWT 구현과 보호 API 필터를 분리한다.

**Blocked by:** 01

**Status:** resolved

- [x] 기존 API 계약 및 토큰 정책 보존
- [x] 관련 단위·통합 검사 통과
- [x] 변경 검증 후 커밋

검증: LoginUseCaseTest, SignupUseCaseTest, AuthApiTest, AuthBoundaryTest 통과. 기존 JWT 키를 공유하는 임시 연결은 티켓 06에서 제거한다.
