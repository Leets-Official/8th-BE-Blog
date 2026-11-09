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
    public static final String BEARER = "JWT";

    @Bean
    public OpenAPI swagger() {
        Info info = new Info()
                .title("Leets 8기 BE Blog API")
                .description("""
                        JWT(Access Token)로 회원을 인증합니다.

                        1. `POST /auth/signup`으로 회원가입합니다.
                        2. `POST /auth/login`으로 로그인해 응답의 `accessToken`을 복사합니다.
                        3. 오른쪽 위 **Authorize** 버튼에 accessToken을 넣으면(`Bearer ` 없이 토큰만) \
                        자물쇠가 표시된 API에 `Authorization: Bearer {accessToken}` 헤더가 자동으로 붙습니다.
                        4. Access Token이 만료되면(401 `EXPIRED_TOKEN`) `POST /auth/reissue`에 refreshToken을 보내 \
                        새 토큰을 받습니다. 한 번 쓴 refreshToken은 다시 쓸 수 없습니다.
                        5. 관리자 API(게시글 숨김, 신고 처리)는 role이 ADMIN인 회원만 호출할 수 있습니다. \
                        회원가입으로는 ADMIN을 만들 수 없으므로 DB에서 `UPDATE members SET role = 'ADMIN' WHERE id = 1;`처럼 \
                        바꾼 뒤 **다시 로그인**해야 토큰에 반영됩니다.
                        """)
                .version("0.0.1");

        // Authorization: Bearer {JWT}
        SecurityScheme jwt = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("로그인 응답의 accessToken");

        return new OpenAPI().info(info)
                .addServersItem(new Server().url("/"))
                .components(new Components()
                        .addSecuritySchemes(BEARER, jwt)
                        .schemas(ModelConverters.getInstance(true).readAll(ErrorResponse.class)));
    }
}
