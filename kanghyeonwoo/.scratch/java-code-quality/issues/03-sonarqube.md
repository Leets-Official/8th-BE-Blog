# 03: SonarQube 분석 설정

**What to build:** 환경변수로 서버 주소와 토큰을 제공하여 Gradle에서 SonarQube 분석을 실행한다.

**Blocked by:** 01, 02

**Status:** resolved

- [x] 인증 정보를 저장하지 않고 분석 설정과 실행 방법을 제공한다.
- [x] 서버 연결이 가능하면 분석 업로드 성공을 확인한다.

로컬 `.env` 자동 로드를 추가하고 `./gradlew sonar` 성공을 확인했다. 분석 중 미해결 타입·import 및 preview 기능 경고가 발생했다. 서버의 품질 게이트 결과는 별도로 확인해야 한다. 이전 전체 `clean check`는 통과했다.
