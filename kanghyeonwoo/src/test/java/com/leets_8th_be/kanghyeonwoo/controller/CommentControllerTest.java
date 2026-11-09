package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.entity.*;
import com.leets_8th_be.kanghyeonwoo.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentControllerTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entities;

    @Test
    void listsFlatCommentsAndMasksHiddenOrDeletedContentBeforeSearching() throws Exception {
        User author = users.save(User.create("댓글 작성자", "comment-author@example.com"));
        Post post = posts.save(Post.create("게시글", "본문", author));
        Comment parent = comments.saveAndFlush(Comment.create("질문 부모", author, post));
        Comment child = comments.saveAndFlush(Comment.builder().content("질문 답변 100%")
                .user(author).post(post).parent(parent).build());
        Comment hidden = comments.saveAndFlush(Comment.create("질문 숨길 원문", author, post));
        jdbc.update("update comments set status='HIDDEN' where id=?", hidden.getId());
        Comment deleted = Comment.create("질문 삭제할 원문", author, post);
        deleted.delete();
        comments.saveAndFlush(deleted);
        Post another = posts.save(Post.create("다른 게시글", "본문", author));
        comments.saveAndFlush(Comment.create("질문 다른 글", author, another));

        entities.clear();
        mvc.perform(get("/api/v2/posts/{id}/comments", post.getId()).param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.items[0].deleted").value(true))
                .andExpect(jsonPath("$.data.items[0].content").value("삭제된 댓글입니다."))
                .andExpect(jsonPath("$.data.items[1].status").value("HIDDEN"))
                .andExpect(jsonPath("$.data.items[1].content").value("숨김 처리된 댓글입니다."));
        mvc.perform(get("/api/v2/posts/{id}/comments", post.getId()).param("keyword", "질문").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.items[0].id").value(child.getId()))
                .andExpect(jsonPath("$.data.items[0].parentId").value(parent.getId()));
        mvc.perform(get("/api/v2/posts/{id}/comments", post.getId()).param("keyword", "100%"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get("/api/v2/posts/{id}/comments", post.getId()).param("page", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/posts/999999/comments")).andExpect(status().isNotFound());
    }
}
