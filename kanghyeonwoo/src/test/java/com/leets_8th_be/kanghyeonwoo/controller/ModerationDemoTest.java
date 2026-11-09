package com.leets_8th_be.kanghyeonwoo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiErrorCodes;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootTest
@org.springframework.security.test.context.support.WithMockUser(username = "1", roles = "ADMIN")
@AutoConfigureMockMvc
@ActiveProfiles("moderation-demo")
class ModerationDemoTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mappings;

    @Test
    void everyRegisteredProjectApiDeclaresItsErrorCodes() {
        var projectApis = mappings.getHandlerMethods().entrySet().stream()
                .filter(entry ->
                        entry.getValue().getBeanType().getPackageName().startsWith("com.leets_8th_be.kanghyeonwoo."))
                .filter(entry ->
                        AnnotatedElementUtils.hasAnnotation(entry.getValue().getBeanType(), RestController.class))
                .toList();

        assertThat(projectApis).isNotEmpty();
        var missingDeclarations = projectApis.stream()
                .filter(entry -> !entry.getValue().hasMethodAnnotation(ApiErrorCodes.class))
                .map(entry -> entry.getKey() + " -> " + entry.getValue().getShortLogMessage())
                .toList();
        assertThat(missingDeclarations).as("@ApiErrorCodes가 누락된 프로젝트 API").isEmpty();
    }

    @Test
    void errorExamplesContainOnlyErrorsOfEachOperation() throws Exception {
        String specification = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var document = new ObjectMapper().readTree(specification);
        var expected = Map.ofEntries(
                Map.entry("post /api/v2/posts", "INVALID_INPUT_VALUE MISSING_REQUEST_BODY USER_NOT_FOUND"),
                Map.entry("get /api/v2/posts/{postId}", "TYPE_MISMATCH POST_NOT_FOUND"),
                Map.entry(
                        "patch /api/v2/posts/{postId}",
                        "INVALID_INPUT_VALUE TYPE_MISMATCH MISSING_REQUEST_BODY"
                                + " POST_ACCESS_DENIED POST_NOT_FOUND"),
                Map.entry(
                        "delete /api/v2/posts/{postId}",
                        "INVALID_INPUT_VALUE TYPE_MISMATCH POST_ACCESS_DENIED" + " POST_NOT_FOUND"),
                Map.entry("get /api/v2/posts/{postId}/comments", "INVALID_INPUT_VALUE TYPE_MISMATCH POST_NOT_FOUND"),
                Map.entry(
                        "post /api/v2/posts/{postId}/reports",
                        "INVALID_INPUT_VALUE TYPE_MISMATCH MISSING_REQUEST_BODY"
                                + " SELF_REPORT_FORBIDDEN USER_NOT_FOUND POST_NOT_FOUND"
                                + " DUPLICATE_RESOURCE"),
                Map.entry(
                        "post /api/v2/comments/{commentId}/reports",
                        "INVALID_INPUT_VALUE TYPE_MISMATCH MISSING_REQUEST_BODY"
                                + " SELF_REPORT_FORBIDDEN USER_NOT_FOUND POST_NOT_FOUND"
                                + " COMMENT_NOT_FOUND DUPLICATE_RESOURCE RESOURCE_DELETED"),
                Map.entry("get /api/v2/reports", "INVALID_INPUT_VALUE TYPE_MISMATCH"),
                Map.entry(
                        "patch /api/v2/reports/{reportId}/resolve",
                        "INVALID_INPUT_VALUE TYPE_MISMATCH MISSING_REQUEST_BODY"
                                + " USER_NOT_FOUND REPORT_NOT_FOUND REPORT_ALREADY_RESOLVED"
                                + " RESOURCE_DELETED"),
                Map.entry("get /api/v2/search/keywords/{keywordId}/stats", "TYPE_MISMATCH SEARCH_KEYWORD_NOT_FOUND"),
                Map.entry("get /api/v2/search/trends", ""),
                Map.entry("get /api/v2/search/suggestions", "INVALID_INPUT_VALUE"),
                Map.entry("get /api/v2/posts", "INVALID_INPUT_VALUE TYPE_MISMATCH"),
                Map.entry("get /api/v2/health", ""));

        expected.forEach((operation, errors) -> {
            String[] parts = operation.split(" ", 2);
            var responses = document.path("paths").path(parts[1]).path(parts[0]).path("responses");
            assertThat(responses.isObject()).as(operation).isTrue();
            var actualCodes = new ArrayList<String>();
            responses.properties().forEach(response -> {
                if (Integer.parseInt(response.getKey()) < 400) return;
                var media = response.getValue().path("content").path("application/json");
                assertThat(media.path("schema").path("$ref").asText())
                        .as(operation)
                        .isEqualTo("#/components/schemas/ApiResponseVoid");
                var examples = media.path("examples");
                assertThat(examples.isObject()).as(operation).isTrue();
                examples.properties().forEach(example -> {
                    var code = ApiResponseCode.valueOf(example.getKey());
                    assertThat(response.getKey())
                            .as(operation)
                            .isEqualTo(String.valueOf(code.getHttpStatus().value()));
                    var value = example.getValue().path("value");
                    assertThat(value.path("code").asText()).isEqualTo(code.name());
                    assertThat(value.path("message").asText()).isEqualTo(code.getDefaultMessage());
                    assertThat(value.path("success").asBoolean(true)).isFalse();
                    assertThat(value.has("data") && value.path("data").isNull()).isTrue();
                    actualCodes.add(code.name());
                });
            });
            var expectedCodes = new ArrayList<String>();
            if (!errors.isEmpty()) expectedCodes.addAll(List.of(errors.split(" ")));
            expectedCodes.add("INTERNAL_SERVER_ERROR");
            assertThat(actualCodes).as(operation).containsExactlyInAnyOrderElementsOf(expectedCodes);
        });
        assertThat(document.path("info").path("description").asText())
                .contains("HTTP 404", "ENDPOINT_NOT_FOUND", "HTTP 405", "METHOD_NOT_ALLOWED");
    }

    @Test
    void demoProvidesAPostAndCommentsWithoutAdministratorSetup() throws Exception {
        var result = mvc.perform(get("/api/v2/posts").param("keyword", "신고 데모"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number postId = com.jayway.jsonpath.JsonPath.read(result, "$.data.items[0].id");
        mvc.perform(get("/api/v2/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3));
        mvc.perform(get("/api/v2/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/comments/{commentId}/reports'].post")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports/{reportId}/resolve'].patch")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/comments'].get")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports'].get").exists());
    }

    @Test
    void openApiDocumentsEveryCurrentV2EndpointAndOmitsV1() throws Exception {
        String specification = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("2.0.0"))
                .andExpect(jsonPath("$.paths.length()", org.hamcrest.Matchers.is(20)))
                .andExpect(jsonPath("$.components.securitySchemes.BearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v2/auth/signup'].post.responses['409']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/auth/kakao/start'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/comments/{commentId}'].patch")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/comments'].get")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/comments/{commentId}/reports'].post")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports/{reportId}/resolve'].patch")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/keywords/{keywordId}/stats'].get")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/trends'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/suggestions'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/health'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post.responses['201']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post.responses['400']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post.responses['409']")
                        .exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var codes = JsonPath.<List<String>>read(specification, "$.components.schemas.ApiResponseCode.enum");
        assertThat(codes).contains("SUCCESS", "RESOURCE_CREATED", "POST_NOT_FOUND", "ENDPOINT_NOT_FOUND");
        var requiredPostFields =
                JsonPath.<List<String>>read(specification, "$.components.schemas.PostCreateRequest.required");
        assertThat(requiredPostFields).containsExactlyInAnyOrder("title", "content");
        assertThat(JsonPath.<Integer>read(
                        specification, "$.components.schemas.PostCreateRequest.properties.title.maxLength"))
                .isEqualTo(150);
        assertThat(JsonPath.<List<String>>read(
                        specification, "$.components.schemas.ReportResponse.properties.status.enum"))
                .containsExactly("PENDING", "RESOLVED");
        assertThat(JsonPath.<String>read(specification, "$.components.schemas.ApiResponseVoid.properties.code.$ref"))
                .isEqualTo("#/components/schemas/ApiResponseCode");
        assertThat(
                        JsonPath.<String>read(
                                specification,
                                "$.paths['/api/v2/posts'].post.responses['400'].content['application/json'].examples.INVALID_INPUT_VALUE.value.code"))
                .isEqualTo("INVALID_INPUT_VALUE");
        assertThat(
                        JsonPath.<String>read(
                                specification,
                                "$.paths['/api/v2/posts/{postId}/reports'].post.responses['403'].content['application/json'].examples.SELF_REPORT_FORBIDDEN.value.code"))
                .isEqualTo("SELF_REPORT_FORBIDDEN");
        assertThat(
                        JsonPath.<String>read(
                                specification,
                                "$.paths['/api/v2/posts/{postId}'].get.responses['404'].content['application/json'].examples.POST_NOT_FOUND.value.code"))
                .isEqualTo("POST_NOT_FOUND");
        assertThat(
                        JsonPath.<String>read(
                                specification,
                                "$.paths['/api/v2/posts/{postId}/reports'].post.responses['409'].content['application/json'].examples.DUPLICATE_RESOURCE.value.code"))
                .isEqualTo("DUPLICATE_RESOURCE");
        assertThat(
                        JsonPath.<String>read(
                                specification,
                                "$.paths['/api/v2/health'].get.responses['500'].content['application/json'].examples.INTERNAL_SERVER_ERROR.value.code"))
                .isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(JsonPath.<String>read(
                        specification, "$.components.schemas.ReportCreateRequest.properties.reason.description"))
                .isNotBlank();

        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"));
    }
}
