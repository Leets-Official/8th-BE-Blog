package com.example.demo;

import org.junit.jupiter.api.Test;
import com.example.demo.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SwaggerDocumentationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void documentsPublicEndpointsAndActualSuccessCodes() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var document = mapper.readTree(result.getResponse().getContentAsString());
        var paths = document.path("paths");
        int operations = 0;
        for (var path : paths) operations += path.size();
        assertThat(operations).isEqualTo(12);
        assertThat(paths.has("/health")).isFalse();
        assertThat(paths.has("/string/repeat")).isFalse();
        for (var tag : document.path("tags")) {
            assertThat(tag.path("name").asText()).isNotEqualTo("기본 기능");
        }
        assertThat(paths.has("/posts")).isTrue();
        assertThat(paths.path("/posts").has("get")).isTrue();
        assertThat(paths.has("/post")).isFalse();
        assertThat(paths.has("/post/{postId}")).isFalse();
        assertThat(paths.has("/post/list")).isFalse();
        var detail = paths.path("/posts/{postId}").path("get");
        assertThat(detail.path("tags").get(0).asText()).isEqualTo("게시글");
        var parameters = detail.path("parameters");
        assertThat(parameters).hasSize(2);
        for (var parameter : parameters) {
            assertThat(parameter.path("description").asText()).isNotBlank();
            assertThat(parameter.path("example").asText()).isEqualTo("1");
            if (parameter.path("name").asText().equals("X-User-Id")) {
                assertThat(parameter.path("in").asText()).isEqualTo("header");
                assertThat(parameter.path("required").asBoolean()).isFalse();
            }
        }
        assertThat(paths.path("/posts").path("post").path("responses").has("201")).isTrue();
        assertThat(paths.path("/posts/{postId}").path("delete").path("responses").has("204")).isTrue();
        assertThat(paths.path("/posts/{postId}/reports").path("post").path("responses").has("201")).isTrue();
        for (var path : paths) {
            for (var operation : path) assertThat(operation.path("summary").asText()).isNotBlank();
        }
        assertThat(document.at("/components/schemas/PostCreateRequest/properties/title/description").asText()).isNotBlank();
    }

    @Test
    void documentsRequestAndSuccessResponseDetails() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var doc = mapper.readTree(result.getResponse().getContentAsString());
        var schemas = doc.at("/components/schemas");
        for (String name : new String[] {"PostCreateRequest", "PostUpdateRequest", "ReportCreateRequest",
                "PostResponse", "PostListResponse", "LikeResponse", "ReportResponse"}) {
            var properties = schemas.path(name).path("properties");
            assertThat(properties.size()).isPositive();
            for (var field : properties) assertThat(field.path("description").asText()).isNotBlank();
        }
        var create = schemas.path("PostCreateRequest");
        assertThat(create.path("required").toString()).contains("title", "content").doesNotContain("isPrivate");
        assertThat(create.at("/properties/title/maxLength").asInt()).isEqualTo(50);
        assertThat(schemas.at("/ReportCreateRequest/properties/reason/maxLength").asInt()).isEqualTo(255);
        for (var path : doc.path("paths")) {
            for (var operation : path) {
                for (var parameter : operation.path("parameters")) {
                    assertThat(parameter.path("in").asText()).isIn("path", "header");
                    assertThat(parameter.path("description").asText()).isNotBlank();
                }
                var responseEntries = operation.path("responses").fields();
                while (responseEntries.hasNext()) {
                    var entry = responseEntries.next();
                    if (entry.getKey().equals("204")) {
                        assertThat(entry.getValue().has("content")).isFalse();
                    } else if (entry.getKey().startsWith("2")) {
                        assertThat(entry.getValue().path("content").size()).isPositive();
                    }
                }
                if (operation.has("requestBody")) {
                    assertThat(operation.at("/requestBody/required").asBoolean()).isTrue();
                    assertThat(operation.at("/requestBody/content/application~1json/schema/$ref").asText()).isNotBlank();
                }
            }
        }
    }

    @Test
    void documentsErrorSchemasAndMatchesActualErrorResponses() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        var document = mapper.readTree(result.getResponse().getContentAsString());
        var paths = document.path("paths");
        for (var path : paths) {
            for (var operation : path) {
                var responses = operation.path("responses");
                var codes = responses.fieldNames();
                while (codes.hasNext()) {
                    String code = codes.next();
                    if (Integer.parseInt(code) >= 400) {
                        assertThat(responses.path(code).at("/content/application~1json/schema/$ref").asText())
                                .isEqualTo("#/components/schemas/ErrorResponse");
                        var examples = responses.path(code).at("/content/application~1json/examples");
                        assertThat(examples.size()).isPositive();
                        for (var example : examples) {
                            var value = example.path("value");
                            var error = ErrorCode.valueOf(value.path("code").asText());
                            assertThat(value.path("status").asInt()).isEqualTo(Integer.parseInt(code));
                            assertThat(value.path("message").asText()).isEqualTo(error.getMessage());
                        }
                    }
                }
            }
        }
        var reports = paths.path("/posts/{postId}/reports").path("post").path("responses");
        assertThat(reports.has("400")).isTrue();
        assertThat(reports.has("403")).isTrue();
        assertThat(reports.has("404")).isTrue();
        assertThat(reports.has("409")).isTrue();
        var errors = document.at("/components/schemas/ErrorResponse/properties");
        assertThat(errors.has("code")).isTrue();
        assertThat(errors.has("message")).isTrue();
        assertThat(errors.has("errors")).isTrue();
        mvc.perform(post("/posts/1/reports").contentType("application/json")
                        .content("{\"reason\":\"spam\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("MISSING_USER_ID"))
                .andExpect(jsonPath("$.message").isString());
        mvc.perform(get("/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void servesSwaggerAndKeepsUndocumentedBasicEndpointsWorking() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("ok"));
        mvc.perform(post("/string/repeat").contentType("application/json").content("{\"value\":\"Leets\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.string_one").value("Leets"))
                .andExpect(jsonPath("$.string_two").value("Leets"));
    }
}
