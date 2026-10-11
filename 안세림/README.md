# 1주차 과제 - 안세림

## 구현 내용
- GET /health : 서버 상태 확인 (ok 반환)
- POST /string/repeat : 입력한 문자열을 string_one, string_two로 반환

## 구조
- controller : 요청/응답 처리
- service : 문자열 처리 로직
- dto : 요청/응답 객체

## 참고
- 이번 과제는 DB를 사용하지 않아 DataSource 자동설정을 제외했습니다.

---

# 2주차 과제 - 안세림

## 구현 내용
- Member, Post, Comment 엔티티 구현
- 생성/수정 시간은 BaseTimeEntity로 분리

## 연관관계
- Post → Member : 다대일 (단방향)
- Comment → Member : 다대일 (단방향)
- Post ↔ Comment : 일대다 / 다대일 (양방향, 게시글 삭제 시 댓글도 함께 삭제)

## ERD에서 바꾼 부분
- Comment PK 타입 INT → BIGINT로 통일
- adminKey 제외 (참조 테이블 없음, 인증/권한은 과제 범위 아님)
- rdate, mdate → created_at, updated_at (DATETIME)
- 컬럼명 스네이크 케이스로 통일