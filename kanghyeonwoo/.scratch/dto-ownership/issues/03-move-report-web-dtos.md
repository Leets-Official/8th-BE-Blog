# 03: 신고 웹 DTO를 report 웹 어댑터로 이동

**What to build:** 신고 접수·조회·처리 API가 report 기능이 소유하는 요청·응답 모델을 사용하면서 기존 API 계약을 유지한다.

**Blocked by:** None (can start immediately).

**Status:** ready-for-agent

- [ ] 신고 접수·처리 요청과 생성·목록·처리 응답 모델을 report 웹 어댑터가 소유한다.
- [ ] 신고 상태, 대상 종류와 처리 행동의 공개 표현을 report 웹 어댑터가 소유한다.
- [ ] 요청 검증과 기존 JSON 필드·응답 상태가 유지된다.
- [ ] 신고 API 계약 테스트가 통과한다.
