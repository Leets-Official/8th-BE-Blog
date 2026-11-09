# 01: 의미 중심 응답 코드 enum과 공통 응답 계약

**What to build:** API V2 응답 봉투의 `code`를 임의 문자열에서 공통 `ApiResponseCode` enum으로 바꾸고, 코드별 HTTP 상태 매핑을 일관되게 제공한다.

**Blocked by:** None.

**Status:** implemented

- [x] `ApiResponse.code` 타입을 `ApiResponseCode` enum으로 변경한다. JSON에는 enum 이름이 문자열로 직렬화된다.
- [x] 응답 봉투의 `success`, `message`, `data`를 유지하고 실패 시 `data=null`을 유지한다.
- [x] enum은 의미 코드와 HTTP 상태를 매핑하며, 코드 이름에 HTTP 숫자나 일련번호를 포함하지 않는다.
- [x] 성공 코드는 `SUCCESS`(200), `RESOURCE_CREATED`(201)로 구분한다.
- [x] 공통·도메인 코드와 상태는 승인된 spec의 매핑표를 따른다. 중복 신고는 `DUPLICATE_RESOURCE`, 신고 재처리는 `REPORT_ALREADY_RESOLVED`, 삭제 대상 오류는 `RESOURCE_DELETED`다.
- [x] 등록되지 않은 경로는 `ENDPOINT_NOT_FOUND`와 HTTP 404를 반환한다.
- [x] `GlobalExceptionHandler`와 모든 성공 응답 생성 지점에서 enum만 사용한다. 미등록 임의 문자열 코드를 전달할 수 없게 한다.
- [x] 오류 응답은 `ApiResponse<Void>`를 유지하고 메시지 구체화와 관계없이 같은 코드는 같은 HTTP 상태를 사용한다.
- [x] 단위·웹 계층 테스트로 enum→JSON 직렬화, 오류별 상태 매핑, 200/201 성공 상태, 오류 data null을 검증한다.
