package com.leets_8th_be.kanghyeonwoo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Import(SearchAnalyticsTest.TimeConfiguration.class)
class SearchDemoTest {
    @Autowired MockMvc mvc;

    @Test
    void insertsExampleDataAndShowsTheExpectedRankings() throws Exception {
        mvc.perform(get("/api/v2/search/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].keyword").value("spring"))
                .andExpect(jsonPath("$.data[0].currentCount").value(10))
                .andExpect(jsonPath("$.data[0].previousCount").value(2))
                .andExpect(jsonPath("$.data[0].riseScore").value(1.6))
                .andExpect(jsonPath("$.data[1].keyword").value("jpa"))
                .andExpect(jsonPath("$.data[2].keyword").value("java"));
        mvc.perform(get("/api/v2/search/suggestions").param("keyword", "스프링부투"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("스프링부트"))
                .andExpect(jsonPath("$.data[1].keyword").value("스프링"));
        String body = mvc.perform(get("/api/v2/search/suggestions").param("keyword", "sprng"))
                .andExpect(jsonPath("$.data[0].recentSearchCount").value(10))
                .andReturn().getResponse().getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(body, "$.data[0].keywordId");
        mvc.perform(get("/api/v2/search/keywords/" + id + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentCount").value(10))
                .andExpect(jsonPath("$.data.previousCount").value(2))
                .andExpect(jsonPath("$.data.hourlyCounts[21].count").value(2))
                .andExpect(jsonPath("$.data.hourlyCounts[22].count").value(10));
        mvc.perform(get("/api/v2/posts")).andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(2));
    }
}
