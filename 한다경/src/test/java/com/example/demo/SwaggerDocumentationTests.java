package com.example.demo;

import org.junit.jupiter.api.Test;
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
    void servesSwaggerAndKeepsUndocumentedBasicEndpointsWorking() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(content().string("ok"));
        mvc.perform(post("/string/repeat").contentType("application/json").content("{\"value\":\"Leets\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.string_one").value("Leets"))
                .andExpect(jsonPath("$.string_two").value("Leets"));
    }
}
