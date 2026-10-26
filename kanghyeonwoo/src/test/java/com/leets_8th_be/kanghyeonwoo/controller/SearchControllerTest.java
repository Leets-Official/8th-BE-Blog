package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SearchControllerTest {
    @Autowired MockMvc mvc;
    @Autowired PostRepository posts;
    @Autowired UserRepository users;

    @Test
    void uncountedAndInvalidRequestsDoNotCreateSessions() throws Exception {
        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
        mvc.perform(get("/posts").param("keyword", "spring").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
        mvc.perform(get("/posts").param("keyword", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
    }

    @Test
    void aCountedFirstPageSearchCreatesASession() throws Exception {
        mvc.perform(get("/posts").param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNotNull());
    }

    @Test
    void searchesNormalizedTitlesWithoutChangingTheirDisplay() throws Exception {
        User author = users.save(User.create("검색 작성자", "search-author@example.com"));
        posts.saveAndFlush(Post.create("Spring   Boot 입문", "본문", author));

        mvc.perform(get("/posts").param("keyword", "  SPRING  BOOT  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Spring   Boot 입문"));
    }

    @Test
    void suggestsAnEnglishTypoFromTheInitialDictionary() throws Exception {
        mvc.perform(get("/search/suggestions").param("keyword", "sprng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("spring"))
                .andExpect(jsonPath("$.data[0].distance").value(1));
    }

    @Test
    void newlySuccessfulSearchesBecomeSuggestible() throws Exception {
        User author = users.save(User.create("사전 작성자", "dictionary@example.com"));
        posts.saveAndFlush(Post.create("Tracerframework 사용법", "본문", author));
        mvc.perform(get("/posts").param("keyword", "Tracerframework")).andExpect(status().isOk());
        mvc.perform(get("/search/suggestions").param("keyword", "tracerframewor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("tracerframework"));
    }

    @Test
    void normalizesKoreanAndTreatsSearchWildcardsLiterally() throws Exception {
        User author = users.save(User.create("한글 작성자", "korean@example.com"));
        posts.saveAndFlush(Post.create("스프링 100% 학습", "본문", author));
        posts.saveAndFlush(Post.create("스프링 100X 학습", "본문", author));
        mvc.perform(get("/posts").param("keyword", "100%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(get("/posts").param("keyword", java.text.Normalizer.normalize("스프링", java.text.Normalizer.Form.NFD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
    }
}
