package com.leets_8th_be.kanghyeonwoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .username("테스터")
                .email("tester@example.com")
                .build());
    }

    @Test
    @DisplayName("신규 게시글을 정상적으로 등록하면 201 Created와 생성된 게시글 데이터를 반환한다.")
    void createPostSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "테스트 제목");
        request.put("content", "테스트 본문 내용입니다.");
        request.put("authorId", testUser.getId());

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S201"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 본문 내용입니다."))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("제목이 누락되거나 빈 문자열이면 400 Bad Request와 COMMON400_VALIDATION을 반환한다.")
    void createPostInvalidTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "   ");
        request.put("content", "본문 내용");
        request.put("authorId", testUser.getId());

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_VALIDATION"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 authorId로 등록을 시도하면 404 Not Found와 USER4001을 반환한다.")
    void createPostUserNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "제목");
        request.put("content", "본문");
        request.put("authorId", 999999L);

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER4001"));
    }
}
