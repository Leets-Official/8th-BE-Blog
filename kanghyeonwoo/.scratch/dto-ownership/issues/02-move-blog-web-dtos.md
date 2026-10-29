# 02: 게시글·댓글 웹 DTO를 blog 웹 어댑터로 이동

**What to build:** 게시글·댓글 API가 blog 기능이 소유하는 요청·응답 모델을 사용하면서 기존 API 계약을 유지한다.

**Blocked by:** None (can start immediately).

**Status:** resolved

- [x] 게시글 생성·수정·조회·삭제 요청과 응답 모델을 blog 웹 어댑터가 소유한다.
- [x] 댓글 조회 응답과 공개 상태 표현을 blog 웹 어댑터가 소유한다.
- [x] 요청 검증과 기존 JSON 필드·응답 상태가 유지된다.
- [x] 게시글·댓글 API 계약 테스트가 통과한다.
