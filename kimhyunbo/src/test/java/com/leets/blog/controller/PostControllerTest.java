package com.leets.blog.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    // data.sql로 미리 넣어둔 회원
    private static final long AUTHOR_ID = 101L;
    private static final long OTHER_MEMBER_ID = 102L;

    @Autowired
    private MockMvc mockMvc;

    private ResultActions createPost(String title, String content, Long authorId) throws Exception {
        String body = """
                {"title": %s, "content": %s, "authorId": %s}
                """.formatted(quote(title), quote(content), authorId);
        return mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long createPostAndGetId(String title) throws Exception {
        String response = createPost(title, "본문", AUTHOR_ID).andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceAll(".*\"data\":\\{\"id\":(\\d+).*", "$1"));
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }

    @Test
    @DisplayName("게시글을 작성하면 201과 S201 코드, 작성된 게시글을 반환한다")
    void createPost() throws Exception {
        createPost("3주차 과제", "본문입니다.", AUTHOR_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S201"))
                .andExpect(jsonPath("$.data.title").value("3주차 과제"))
                .andExpect(jsonPath("$.data.authorId").value(AUTHOR_ID))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("제목이 공백이면 400과 COMMON400_VALIDATION, 잘못된 필드를 반환한다")
    void createPostWithBlankTitle() throws Exception {
        createPost(" ", "본문입니다.", AUTHOR_ID)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_VALIDATION"))
                .andExpect(jsonPath("$.data[0].field").value("title"))
                .andExpect(jsonPath("$.data[0].reason").value("제목은 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("요청 본문이 없으면 400과 COMMON400_BODY를 반환한다")
    void createPostWithoutBody() throws Exception {
        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_BODY"));
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 작성하면 404와 MEMBER4001을 반환한다")
    void createPostWithUnknownMember() throws Exception {
        createPost("제목", "본문", 999L)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER4001"));
    }

    @Test
    @DisplayName("목록을 조회하면 최신순으로 반환하고, keyword로 제목을 검색할 수 있다")
    void getPosts() throws Exception {
        createPostAndGetId("REST API 설계");
        createPostAndGetId("JPA 엔티티");

        mockMvc.perform(get("/posts").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("JPA 엔티티"));

        mockMvc.perform(get("/posts").param("keyword", "REST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("REST API 설계"));
    }

    @Test
    @DisplayName("page가 1보다 작으면 400과 COMMON400_VALIDATION을 반환한다")
    void getPostsWithInvalidPage() throws Exception {
        mockMvc.perform(get("/posts").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_VALIDATION"))
                .andExpect(jsonPath("$.data[0].field").value("page"));
    }

    @Test
    @DisplayName("게시글을 상세 조회하면 200과 상세 내용을 반환한다")
    void getPost() throws Exception {
        long postId = createPostAndGetId("상세 조회");

        mockMvc.perform(get("/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.content").value("본문"))
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 조회하면 404와 POST4001을 반환한다")
    void getPostNotFound() throws Exception {
        mockMvc.perform(get("/posts/{postId}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4001"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("게시글 번호 자리에 문자가 오면 400과 COMMON400_TYPE을 반환한다")
    void getPostWithInvalidType() throws Exception {
        mockMvc.perform(get("/posts/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_TYPE"));
    }

    @Test
    @DisplayName("작성자가 게시글을 수정하면 200과 수정된 게시글을 반환한다")
    void updatePost() throws Exception {
        long postId = createPostAndGetId("수정 전");

        mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 101, "title": "수정 후", "content": "수정된 본문"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.title").value("수정 후"))
                .andExpect(jsonPath("$.data.content").value("수정된 본문"));
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 수정하면 403과 POST4003을 반환한다")
    void updatePostByOtherMember() throws Exception {
        long postId = createPostAndGetId("남의 글");

        mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": %d, "title": "수정 시도", "content": "수정 시도"}
                                """.formatted(OTHER_MEMBER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST4003"));
    }

    @Test
    @DisplayName("작성자가 게시글을 삭제하면 200과 삭제된 게시글 ID를 반환하고, 이후 조회 시 404가 된다")
    void deletePost() throws Exception {
        long postId = createPostAndGetId("삭제할 글");

        mockMvc.perform(delete("/posts/{postId}", postId).param("authorId", String.valueOf(AUTHOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.deletedPostId").value(postId));

        mockMvc.perform(get("/posts/{postId}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST4001"));
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 삭제하면 403과 POST4003을 반환한다")
    void deletePostByOtherMember() throws Exception {
        long postId = createPostAndGetId("남의 글");

        mockMvc.perform(delete("/posts/{postId}", postId).param("authorId", String.valueOf(OTHER_MEMBER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST4003"));
    }

    @Test
    @DisplayName("작성자가 게시글을 숨기면 HIDDEN이 되고, 목록과 상세 조회에서 보이지 않는다")
    void hidePost() throws Exception {
        long postId = createPostAndGetId("숨길 글");

        mockMvc.perform(patch("/posts/{postId}/hide", postId).param("authorId", String.valueOf(AUTHOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        mockMvc.perform(get("/posts/{postId}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST4001"));

        mockMvc.perform(get("/posts").param("keyword", "숨길 글"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("이미 숨긴 게시글을 다시 숨기면 409와 POST4091을 반환한다")
    void hidePostTwice() throws Exception {
        long postId = createPostAndGetId("숨길 글");
        mockMvc.perform(patch("/posts/{postId}/hide", postId).param("authorId", String.valueOf(AUTHOR_ID)));

        mockMvc.perform(patch("/posts/{postId}/hide", postId).param("authorId", String.valueOf(AUTHOR_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POST4091"));
    }

    @Test
    @DisplayName("작성자가 아닌 회원이 숨기면 403과 POST4003을 반환한다")
    void hidePostByOtherMember() throws Exception {
        long postId = createPostAndGetId("남의 글");

        mockMvc.perform(patch("/posts/{postId}/hide", postId).param("authorId", String.valueOf(OTHER_MEMBER_ID)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST4003"));
    }

    @Test
    @DisplayName("지원하지 않는 메서드로 요청하면 405와 COMMON405를 반환한다")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(post("/posts/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("COMMON405"));
    }
}
