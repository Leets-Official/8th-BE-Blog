# 01: 공통 웹 응답 DTO를 shared web으로 이동

**What to build:** 모든 API와 예외 응답이 공통 응답 계약을 사용하면서 현재 JSON 형식을 그대로 반환한다.

**Blocked by:** None (can start immediately).

**Status:** resolved

- [x] 성공·실패 응답 래퍼와 공통 페이지 응답을 shared web이 소유한다.
- [x] 기능 컨트롤러와 예외 응답이 같은 공통 계약을 사용한다.
- [x] 기존 필드명, 직렬화 형식, HTTP 상태와 응답 코드가 바뀌지 않는다.
- [x] 기존 API 계약 테스트가 통과한다.
