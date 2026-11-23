# 02: PMD 규칙 보강

**What to build:** 결함과 유지보수 문제를 발견하는 규칙을 선별하여 적용하고 기존 위반을 정리한다.

**Blocked by:** 없음

**Status:** resolved

- [x] 선택한 규칙과 예외 근거를 문서화한다.
- [x] main과 test 검사가 통과한다.

빈 catch와 객체 및 문자열의 잘못된 참조 비교를 검사한다. 별도 예외는 추가하지 않았다. `pmdMain pmdTest` 통과.
