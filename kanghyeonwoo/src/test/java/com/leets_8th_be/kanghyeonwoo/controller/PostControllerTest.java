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

    @Test
    @DisplayName("게시글 상세 조회 성공 시 200 OK와 상세 정보(updatedAt 포함)를 반환한다.")
    void getPostDetailSuccess() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("상세 제목", "상세 본문", testUser)
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.message").value("게시글 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("상세 제목"))
                .andExpect(jsonPath("$.data.content").value("상세 본문"))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 상세 조회 시 404 Not Found와 POST4001을 반환한다.")
    void getPostDetailNotFound() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4001"))
                .andExpect(jsonPath("$.message").value("해당 ID의 게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("게시글 ID에 문자가 전달되면 400 Bad Request와 COMMON400_TYPE을 반환한다.")
    void getPostDetailTypeMismatch() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/posts/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_TYPE"));
    }

    @Test
    @DisplayName("게시글 목록 조회 시 200 OK와 최신순 정렬된 요약 목록 배열을 반환한다.")
    void getPostsSuccess() throws Exception {
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("첫 번째 글", "본문 1", testUser));
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("두 번째 글", "본문 2", testUser));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/posts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("두 번째 글"))
                .andExpect(jsonPath("$.data[1].title").value("첫 번째 글"));
    }

    @Test
    @DisplayName("키워드로 게시글 목록을 검색하면 일치하는 게시글만 반환한다.")
    void searchPostsByKeyword() throws Exception {
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("Spring REST API", "RESTful 설계", testUser));
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("JPA Study", "엔티티 매핑", testUser));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/posts")
                        .param("keyword", "REST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Spring REST API"));
    }
}
