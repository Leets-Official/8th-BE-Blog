# Java 코드 품질 검사

`./gradlew check`는 Spotless, Checkstyle, PMD와 테스트를 실행한다. `spotlessApply`는 포맷과 사용하지 않는 import를 정리하고, `spotlessCheck`는 검사한다. Checkstyle은 타입 이름, 일반 와일드카드 import와 최상위 타입을 검사하며 static 와일드카드 import는 허용한다. PMD는 설정 파일에 명시된 결함 패턴과 복잡도를 검사한다.

SonarQube 서버에서 프로젝트를 만들고 분석 토큰을 발급한 뒤 프로젝트 디렉터리의 `.env`에 `SONAR_HOST_URL`과 `SONAR_TOKEN`을 저장한다. Gradle이 두 설정을 자동으로 읽으므로 다음 명령만 실행하면 된다. `.env`는 Git에서 제외된다. 프로젝트 키가 서버 설정과 다르면 `-Dsonar.projectKey=서버의키`를 추가한다.

```sh
./gradlew sonar
```

환경변수에 같은 이름이 있으면 `.env`보다 우선한다. 서버 주소의 기본값은 `http://localhost:9000`이다. `.env`는 단순 `KEY=value` 형식과 따옴표로 감싼 값을 지원하며 셸 코드를 실행하지 않는다. SonarQube for IDE 확장과 서버 분석은 각각 별도로 실행된다. 서버의 품질 프로필에 따라 검사 규칙이 결정된다.

PMD 복잡도 경계는 기존 Kakao 응답 처리 메서드의 복잡도 20을 기준으로 메서드 21, 클래스 100으로 설정했다. 기존 코드를 개선하면 경계를 낮출 수 있다.
