package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
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
                .components(new Components()
                        .addSchemas("ApiResponseCode", apiResponseCodeSchema())
                        .addSchemas("ApiResponseVoid", apiResponseVoidSchema())
                        .addResponses("MethodNotAllowed", commonErrorResponse("허용되지 않은 HTTP 메서드입니다."))
                        .addResponses("InternalServerError", commonErrorResponse("처리되지 않은 서버 오류입니다.")))
                .info(new Info()
                        .title("Leets 8th Blog RESTful API")
                        .description("Leets 8기 백엔드 블로그 서비스 API 명세서입니다.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("강현우")
                                .email("wooddang@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }

    private static Schema<?> apiResponseCodeSchema() {
        return enumSchema(ApiResponseCode.values(), "응답 의미 코드");
    }

    private static Schema<?> enumSchema(Enum<?>[] values, String description) {
        StringSchema schema = new StringSchema();
        schema.setDescription(description);
        schema.setEnum(java.util.Arrays.stream(values).map(Enum::name).toList());
        schema.setExample(values.length == 0 ? null : values[0].name());
        return schema;
    }

    private static Schema<?> apiResponseVoidSchema() {
        Schema<?> codeSchema = new Schema<>().$ref("#/components/schemas/ApiResponseCode");
        codeSchema.setExample("POST_NOT_FOUND");
        return new ObjectSchema()
                .description("오류 응답에 사용하는 ApiResponse<Void> 형식입니다.")
                .addProperty("success", new BooleanSchema().description("성공 여부").example(false))
                .addProperty("code", codeSchema)
                .addProperty("message", new StringSchema().description("오류 설명").example("요청한 게시글을 찾을 수 없습니다."))
                .addProperty("data", new Schema<>().description("오류 응답은 null").nullable(true).example(null));
    }

    private static io.swagger.v3.oas.models.responses.ApiResponse commonErrorResponse(String description) {
        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResponseVoid"))));
    }
}
