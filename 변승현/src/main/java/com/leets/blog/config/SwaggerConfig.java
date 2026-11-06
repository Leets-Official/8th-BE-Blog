package com.leets.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI blogOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("블로그 API")
                .version("1.0"));
    }
}
