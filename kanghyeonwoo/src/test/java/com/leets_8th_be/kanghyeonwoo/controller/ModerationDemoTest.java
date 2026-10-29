package com.leets_8th_be.kanghyeonwoo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("moderation-demo")
class ModerationDemoTest {
    @Autowired MockMvc mvc;

    @Test
    void demoProvidesAPostAndCommentsWithoutAdministratorSetup() throws Exception {
        var result = mvc.perform(get("/api/v2/posts").param("keyword", "신고 데모"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andReturn().getResponse().getContentAsString();
        Number postId = com.jayway.jsonpath.JsonPath.read(result, "$.data.items[0].id");
        mvc.perform(get("/api/v2/posts/{id}/comments", postId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(3));
        mvc.perform(get("/api/v2/reports")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/comments/{commentId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports/{reportId}/resolve'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/comments'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports'].get").exists());
    }

    @Test
    void openApiDocumentsEveryCurrentV2EndpointAndOmitsV1() throws Exception {
        String specification = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("2.0.0"))
                .andExpect(jsonPath("$.paths.length()", org.hamcrest.Matchers.is(11)))
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/comments'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/comments/{commentId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/reports/{reportId}/resolve'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/keywords/{keywordId}/stats'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/trends'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/search/suggestions'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/health'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v2/posts/{postId}/reports'].post.responses['409']").exists())
                .andExpect(jsonPath("$.components.responses.InternalServerError").exists())
                .andReturn().getResponse().getContentAsString();

        var codes = JsonPath.<java.util.List<String>>read(specification, "$.components.schemas.ApiResponseCode.enum");
        assertThat(codes).contains("SUCCESS", "RESOURCE_CREATED", "POST_NOT_FOUND", "ENDPOINT_NOT_FOUND");
        var requiredPostFields = JsonPath.<java.util.List<String>>read(specification,
                "$.components.schemas.PostCreateRequest.required");
        assertThat(requiredPostFields).contains("title", "content", "authorId");
        assertThat(JsonPath.<Integer>read(specification,
                "$.components.schemas.PostCreateRequest.properties.title.maxLength")).isEqualTo(150);
        assertThat(JsonPath.<java.util.List<String>>read(specification,
                "$.components.schemas.ReportResponse.properties.status.enum"))
                .containsExactly("PENDING", "RESOLVED");
        assertThat(JsonPath.<String>read(specification,
                "$.components.schemas.ApiResponseVoid.properties.code.$ref"))
                .isEqualTo("#/components/schemas/ApiResponseCode");
        assertThat(JsonPath.<String>read(specification,
                "$.components.schemas.ApiResponseVoid.properties.code.example")).isEqualTo("POST_NOT_FOUND");
        assertThat(JsonPath.<String>read(specification,
                "$.components.schemas.ReportCreateRequest.properties.reason.description")).isNotBlank();

        mvc.perform(get("/api/v1/health"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"));
    }
}
