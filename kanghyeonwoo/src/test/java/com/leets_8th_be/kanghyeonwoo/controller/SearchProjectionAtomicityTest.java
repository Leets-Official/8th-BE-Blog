package com.leets_8th_be.kanghyeonwoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SearchProjectionAtomicityTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired JdbcTemplate jdbc;

    @Test
    void projectionFailureRollsBackPostCreation() throws Exception {
        User author = users.saveAndFlush(User.create("인덱스 오류 작성자", "projection-failure-" + UUID.randomUUID() + "@example.com"));
        String title = "projection-failure-" + UUID.randomUUID();
        jdbc.execute("ALTER TABLE search_post_projections ADD CONSTRAINT test_projection_failure CHECK (title <> '" + title + "')");
        try {
            mvc.perform(post("/api/v1/posts").contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("title", title, "content", "본문", "authorId", author.getId()))))
                    .andExpect(status().isInternalServerError());
            Integer persistedPosts = jdbc.queryForObject("SELECT COUNT(*) FROM posts WHERE title = ?", Integer.class, title);
            assertThat(persistedPosts).isZero();
        } finally {
            jdbc.execute("ALTER TABLE search_post_projections DROP CONSTRAINT test_projection_failure");
        }
    }
}
