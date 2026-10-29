package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI blogOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Leets 5주차 API 문서")
                .version("v1")
                .description("5주차 API 문서입니다. 회원 식별이 필요한 API에는 X-User-Id 헤더를 입력합니다."));
    }
}
