# 04: 검색 웹 DTO를 search 어댑터로 이동하고 전역 DTO 패키지 정리

**What to build:** 검색·게시글 검색 API가 search 기능의 웹 응답 모델을 사용하고, 전역 DTO 패키지가 남지 않는다.

**Blocked by:** 01 공통 웹 응답 DTO 이동, 02 게시글·댓글 웹 DTO 이동, 03 신고 웹 DTO 이동.

**Status:** resolved

- [x] 게시글 검색 목록, 검색어 추천·통계·급상승·시간대별 검색 응답 모델을 search 웹 어댑터가 소유한다.
- [x] 기존 JSON 필드·응답 상태와 OpenAPI 노출이 유지된다.
- [x] 모든 기능별 웹 DTO 이동 후 전역 `dto` 패키지를 제거한다.
- [x] 아키텍처 검사가 DTO의 기능별 소유권을 지키고 API 계약 테스트가 통과한다.
