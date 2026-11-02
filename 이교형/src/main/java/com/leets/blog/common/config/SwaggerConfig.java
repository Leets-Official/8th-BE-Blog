package com.leets.blog.common.config;

import com.leets.blog.common.swagger.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // @SecurityRequirement(name = ...)에서 참조하는 이름
    public static final String MEMBER_ID = "X-Member-Id";

    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("Leets 8기 BE Blog API")
                .description("""
                        로그인 대신 `X-Member-Id` 헤더로 회원을 구분합니다.

                        1. `POST /members`로 회원가입 후 응답의 `memberId`를 확인합니다.
                        2. 오른쪽 위 **Authorize** 버튼에 그 값을 넣으면 자물쇠가 표시된 API에 헤더가 자동으로 붙습니다.
                        3. 관리자 API(게시글 숨김, 신고 처리)는 role이 ADMIN인 회원 ID가 필요합니다. \
                        회원가입으로는 ADMIN을 만들 수 없으므로 DB에서 `UPDATE members SET role = 'ADMIN' WHERE id = 1;`처럼 변경합니다.
                        """)
                .version("0.0.1");

        // 로그인이 없어 Bearer(JWT) 대신 헤더 값을 그대로 보내는 apiKey 방식
        SecurityScheme memberId = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Member-Id")
                .description("회원가입 응답의 memberId");

        return new OpenAPI().info(info)
                .addServersItem(new Server().url("/"))
                .components(new Components()
                        .addSecuritySchemes(MEMBER_ID, memberId)
                        .schemas(ModelConverters.getInstance(true).readAll(ErrorResponse.class)));
    }
}
