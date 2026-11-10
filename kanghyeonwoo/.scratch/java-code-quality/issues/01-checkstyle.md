# 01: Checkstyle 코딩 규칙 검사

**What to build:** 이름과 import 등 팀 코딩 규칙을 Gradle 검사에 적용하고 기존 위반을 정리한다.

**Blocked by:** 없음

**Status:** resolved

- [x] main과 test 코드에서 규칙 위반을 검사한다.
- [x] Spotless와 중복되지 않는 규칙을 Gradle check에 연결한다.

일반 와일드카드 import와 파일 내 여러 최상위 타입을 검사한다. 테스트 라이브러리에서 쓰는 static 와일드카드 import는 허용한다. `checkstyleMain checkstyleTest` 통과.
