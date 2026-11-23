FROM eclipse-temurin:21-jre AS dependencies
WORKDIR /build
COPY build/libs/app.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --destination extracted

FROM eclipse-temurin:21-jre AS h2
WORKDIR /var/lib/blog
COPY --from=dependencies /build/extracted/lib/h2-*.jar /opt/h2.jar
RUN chown 10001:10001 /var/lib/blog
USER 10001:10001

FROM eclipse-temurin:21-jre AS app
WORKDIR /opt/blog
COPY --chmod=0444 build/libs/app.jar app.jar
RUN mkdir data && chown 10001:10001 data
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
