package com.leets.choyeji;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets.choyeji.auth.JwtTokenService;
import com.leets.choyeji.entity.Comment;
import com.leets.choyeji.entity.Member;
import com.leets.choyeji.entity.Post;
import com.leets.choyeji.repository.CommentRepository;
import com.leets.choyeji.repository.MemberRepository;
import com.leets.choyeji.repository.PostRepository;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:comment-api;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=integration-test-secret-32-bytes-long"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired CommentRepository comments;
    @Autowired PostRepository posts;
    @Autowired MemberRepository members;
    @Autowired JwtTokenService tokens;
    private Member owner;
    private Member commenter;
    private Post post;
    private String authorization;

    @BeforeEach
    void setUp() {
        comments.deleteAll();
        posts.deleteAll();
        members.deleteAll();
        owner = members.save(new Member("unused", "글작성자", "owner@example.com"));
        commenter = members.save(new Member("unused", "댓글작성자", "commenter@example.com"));
        post = posts.save(new Post(owner, "게시글", "내용"));
        authorization = "Bearer " + tokens.issue(commenter.getId());
    }

    @Test
    void authenticatedMemberCanCommentOnAnotherMembersPost() throws Exception {
        String response = mvc.perform(post("/posts/{id}/comments", post.getId())
                        .header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON)
                        .content(body("댓글 내용")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.commentId").isNumber())
                .andExpect(jsonPath("$.data.postId").value(post.getId()))
                .andExpect(jsonPath("$.data.content").value("댓글 내용"))
                .andExpect(jsonPath("$.data.writer").value("댓글작성자"))
                .andExpect(jsonPath("$.data.createdDate").value(LocalDate.now().toString()))
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(response).path("data").path("commentId").asLong();
        Comment saved = comments.findById(id).orElseThrow();
        assertThat(saved.getMember().getId()).isEqualTo(commenter.getId());
        assertThat(saved.getPost().getId()).isEqualTo(post.getId());
        mvc.perform(get("/posts/{id}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].commentId").value(id));
        mvc.perform(delete("/posts/{id}", post.getId())
                        .header("Authorization", "Bearer " + tokens.issue(owner.getId())))
                .andExpect(status().isOk());
        assertThat(comments.count()).isZero();
    }

    @Test
    void publicListIsOrderedAndExcludesOtherPostsComments() throws Exception {
        Comment first = comments.save(new Comment(post, commenter, "첫 댓글"));
        Comment second = comments.save(new Comment(post, owner, "두 번째 댓글"));
        Post other = posts.save(new Post(owner, "다른 게시글", "내용"));
        comments.save(new Comment(other, commenter, "다른 글 댓글"));
        mvc.perform(get("/posts/{id}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].commentId").value(first.getId()))
                .andExpect(jsonPath("$.data[0].writer").value("댓글작성자"))
                .andExpect(jsonPath("$.data[1].commentId").value(second.getId()))
                .andExpect(jsonPath("$.data[1].writer").value("글작성자"));
    }

    @Test
    void emptyPostReturnsEmptyListAndMissingPostReturns404() throws Exception {
        mvc.perform(get("/posts/{id}/comments", post.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(get("/posts/999999/comments"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POST_404_1"));
        mvc.perform(post("/posts/999999/comments").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body("댓글")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POST_404_1"));
        assertThat(comments.count()).isZero();
    }

    @Test
    void writingRequiresValidTokenAndExistingMember() throws Exception {
        mvc.perform(post("/posts/{id}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(body("댓글")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        for (String bearer : new String[]{"Bearer invalid", "Bearer " + tokens.issue(999999L)}) {
            mvc.perform(post("/posts/{id}/comments", post.getId()).header("Authorization", bearer)
                            .contentType(MediaType.APPLICATION_JSON).content(body("댓글")))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH_401_1"));
        }
        assertThat(comments.count()).isZero();
    }

    @Test
    void invalidContentIsRejectedWithoutSaving() throws Exception {
        for (String input : new String[]{"{}", "{", "{\"content\":null}",
                body(""), body(" \n\t"), body("가".repeat(10001))}) {
            mvc.perform(post("/posts/{id}/comments", post.getId()).header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON).content(input))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("COMMON_400_1"));
        }
        assertThat(comments.count()).isZero();
        mvc.perform(post("/posts/{id}/comments", post.getId()).header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body("가".repeat(10000))))
                .andExpect(status().isOk());
    }

    private String body(String content) throws Exception {
        return mapper.writeValueAsString(Map.of("content", content));
    }
}
