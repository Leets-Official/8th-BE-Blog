# 02: 회원가입 새 경계로 이전

**What to build:** 가입 입력 포트와 계정·암호화 어댑터를 연결하고 웹 계층에서 업무 오류를 HTTP로 변환한다.

**Blocked by:** 01

**Status:** resolved

- [x] 기존 API 계약 및 토큰 정책 보존
- [x] 관련 단위·통합 검사 통과
- [x] 변경 검증 후 커밋

검증: SignupUseCaseTest, AuthApiTest, AuthBoundaryTest 통과.
