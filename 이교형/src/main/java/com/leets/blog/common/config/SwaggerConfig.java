package com.leets.blog.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("Leets 8기 BE Blog API")
                .description("Leets Study API 명세서")
                .version("0.0.1");

        return new OpenAPI().info(info)
                .addServersItem(new Server().url("/"));
    }
}