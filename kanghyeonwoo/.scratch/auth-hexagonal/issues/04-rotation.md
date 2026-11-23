# 04: 재발급 로그아웃과 트랜잭션 분리

**What to build:** 로그인 상태 모델로 회전·폐기를 관리하고 저장소 잠금 및 거절 시 커밋을 유지한다.

**Blocked by:** 03

**Status:** resolved

- [x] 기존 API 계약 및 토큰 정책 보존
- [x] 관련 단위·통합 검사 통과
- [x] 변경 검증 후 커밋

검증: SessionUseCaseTest, AuthApiTest, AuthBoundaryTest 통과. 재사용 예외만 rollback 대상에서 제외해 폐기를 커밋한다.
