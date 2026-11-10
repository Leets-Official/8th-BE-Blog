# 05: 카카오 로그인 가입 이전

**What to build:** 외부 REST·티켓 저장·리다이렉트 어댑터와 카카오 유스케이스를 분리한다.

**Blocked by:** 03

**Status:** resolved

- [x] 기존 API 계약 및 토큰 정책 보존
- [x] 관련 단위·통합 검사 통과
- [x] 변경 검증 후 커밋

검증: KakaoUseCaseTest, KakaoApiTest, KakaoClientTest, AuthApiTest, AuthBoundaryTest 통과. 리다이렉트 URL 조립은 웹 어댑터로 이동했다.
