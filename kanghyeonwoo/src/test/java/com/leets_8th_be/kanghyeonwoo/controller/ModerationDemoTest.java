package com.leets_8th_be.kanghyeonwoo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("moderation-demo")
class ModerationDemoTest {
    @Autowired MockMvc mvc;

    @Test
    void demoProvidesAPostAndCommentsWithoutAdministratorSetup() throws Exception {
        var result = mvc.perform(get("/posts").param("keyword", "신고 데모"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1))
                .andReturn().getResponse().getContentAsString();
        Number postId = com.jayway.jsonpath.JsonPath.read(result, "$.data.items[0].id");
        mvc.perform(get("/posts/{id}/comments", postId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(3));
        mvc.perform(get("/reports")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/posts/{postId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/comments/{commentId}/reports'].post").exists())
                .andExpect(jsonPath("$.paths['/reports/{reportId}/resolve'].patch").exists())
                .andExpect(jsonPath("$.paths['/posts/{postId}/comments'].get").exists())
                .andExpect(jsonPath("$.paths['/reports'].get").exists());
    }
}
