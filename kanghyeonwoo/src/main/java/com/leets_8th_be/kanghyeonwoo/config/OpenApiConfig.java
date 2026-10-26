package com.leets_8th_be.kanghyeonwoo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Leets 8th Blog RESTful API")
                        .description("Leets 8기 백엔드 블로그 서비스 API 명세서입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("강현우")
                                .email("wooddang@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }
}
