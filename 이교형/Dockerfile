# 1단계: 빌드 (JDK + Gradle로 bootJar 생성)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 빌드 설정 파일 먼저 복사 → 소스만 바뀌면 Gradle 배포판·의존성 메타데이터 레이어는 캐시 재사용
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# 2단계: 실행 (JRE만 담아 이미지 크기 줄이기)
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Seoul
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
