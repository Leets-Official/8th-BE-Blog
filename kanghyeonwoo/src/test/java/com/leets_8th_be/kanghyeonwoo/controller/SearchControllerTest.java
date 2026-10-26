package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.user.domain.model.UserId;
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
    @Autowired CreatePostUseCase createPost;

    @Test
    void uncountedAndInvalidRequestsDoNotCreateSessions() throws Exception {
        mvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
        mvc.perform(get("/api/v1/posts").param("keyword", "spring").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
        mvc.perform(get("/api/v1/posts").param("keyword", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNull());
    }

    @Test
    void aCountedFirstPageSearchCreatesASession() throws Exception {
        mvc.perform(get("/api/v1/posts").param("keyword", "spring"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(
                        result.getRequest().getSession(false)).isNotNull());
    }

    @Test
    void searchesNormalizedTitlesWithoutChangingTheirDisplay() throws Exception {
        User author = users.save(User.create("검색 작성자", "search-author@example.com"));
        createPost.create(new CreatePostCommand("Spring   Boot 입문", "본문", new UserId(author.getId())));

        mvc.perform(get("/api/v1/posts").param("keyword", "  SPRING  BOOT  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("Spring   Boot 입문"));
    }

    @Test
    void suggestsAnEnglishTypoFromTheInitialDictionary() throws Exception {
        mvc.perform(get("/api/v1/search/suggestions").param("keyword", "sprng"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("spring"))
                .andExpect(jsonPath("$.data[0].distance").value(1));
    }

    @Test
    void newlySuccessfulSearchesBecomeSuggestible() throws Exception {
        User author = users.save(User.create("사전 작성자", "dictionary@example.com"));
        createPost.create(new CreatePostCommand("Tracerframework 사용법", "본문", new UserId(author.getId())));
        mvc.perform(get("/api/v1/posts").param("keyword", "Tracerframework")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/search/suggestions").param("keyword", "tracerframewor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].keyword").value("tracerframework"));
    }

    @Test
    void normalizesKoreanAndTreatsSearchWildcardsLiterally() throws Exception {
        User author = users.save(User.create("한글 작성자", "korean@example.com"));
        createPost.create(new CreatePostCommand("스프링 100% 학습", "본문", new UserId(author.getId())));
        createPost.create(new CreatePostCommand("스프링 100X 학습", "본문", new UserId(author.getId())));
        mvc.perform(get("/api/v1/posts").param("keyword", "100%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(1));
        mvc.perform(get("/api/v1/posts").param("keyword", java.text.Normalizer.normalize("스프링", java.text.Normalizer.Form.NFD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items.length()").value(2));
    }
}
