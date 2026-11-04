package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leets 8th BE Blog API")
                        .version("v1.0.0")
                        .description("""
                                게시글 / 댓글 / 신고 API 문서입니다.

                                - 모든 응답은 공통 형식 `{ isSuccess, code, message, data }` 으로 내려갑니다.
                                - 인증 도입 전까지 요청자는 `userId` 로 식별합니다. (서버 실행 시 userId 1, 2 가 자동 생성됩니다)
                                - 에러 응답의 `code` 값으로 실패 원인을 구분할 수 있습니다. (예: `REPORT_409_1`)
                                """));
    }
}
