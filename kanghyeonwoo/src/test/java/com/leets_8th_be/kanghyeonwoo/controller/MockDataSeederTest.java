package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.config.MockDataSeeder;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import com.leets_8th_be.kanghyeonwoo.entity.CommentStatus;
import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import org.springframework.transaction.annotation.Transactional;
import com.leets_8th_be.kanghyeonwoo.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock-data")
class MockDataSeederTest {
    @Autowired MockMvc mvc;
    @Autowired MockDataSeeder seeder;
    @Autowired UserRepository users;
    @Autowired PostRepository posts;
    @Autowired CommentRepository comments;
    @Autowired ReportRepository reports;

    @Test
    void createsSearchableFixturesAndDoesNotDuplicateThemWhenRunAgain() throws Exception {
        seeder.run(new DefaultApplicationArguments());
        assertThat(users.count()).isEqualTo(5);
        assertThat(posts.count()).isEqualTo(12);
        assertThat(comments.count()).isEqualTo(36);
        assertThat(reports.count()).isZero();
        mvc.perform(get("/api/v1/posts").param("keyword", "MOCK").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(4));
        assertThat(users.findAll()).allSatisfy(user -> {
            assertThat(user.getUsername()).isNotBlank().hasSizeLessThanOrEqualTo(50);
            assertThat(user.getEmail()).matches("mock-user-[1-5]@blog\\.example");
            assertThat(user.getUsername()).matches(".*[가-힣].*");
        });
        var examples = posts.findAll();
        assertThat(examples).allSatisfy(post -> {
            assertThat(post.getTitle()).startsWith("[MOCK] ").hasSizeLessThanOrEqualTo(150);
            assertThat(post.getContent()).isNotBlank();
        });
        assertThat(examples).filteredOn(post -> post.getStatus() == PostStatus.HIDDEN).hasSize(2);
        assertThat(comments.findAll()).filteredOn(comment -> comment.getStatus() == CommentStatus.HIDDEN).hasSize(2);
        assertThat(comments.findAll()).filteredOn(comment -> comment.isDeleted()).hasSize(2);
        assertThat(comments.findAll()).filteredOn(comment -> comment.getParent() != null).hasSize(12);
        var activePostIds = examples.stream().filter(post -> post.getStatus() == PostStatus.ACTIVE)
                .map(Post::getId).toList();
        var hiddenParent = comments.findAll().stream()
                .filter(comment -> comment.getStatus() == CommentStatus.HIDDEN)
                .filter(comment -> activePostIds.contains(comment.getPost().getId())).findFirst().orElseThrow();
        Long visiblePostId = hiddenParent.getPost().getId();
        mvc.perform(get("/api/v1/posts/{id}/comments", visiblePostId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.items[?(@.parentId == " + hiddenParent.getId() + ")].id").isNotEmpty())
                .andExpect(jsonPath("$.data.items[?(@.status == 'HIDDEN')].content")
                        .value(org.hamcrest.Matchers.contains("숨김 처리된 댓글입니다.")));
        mvc.perform(get("/api/v1/posts/{id}/comments", visiblePostId).param("keyword", "질문"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(0));
        var hidden = examples.stream().filter(p -> p.getStatus() == PostStatus.HIDDEN).findFirst().orElseThrow();
        mvc.perform(get("/api/v1/posts/{id}", hidden.getId())).andExpect(status().isNotFound());
    }
    @Test
    @Transactional
    void preservesExistingContentWhenAddingTheMockDataset() {
        posts.deleteAll();
        users.deleteAll();
        users.flush();
        User originalUser = users.save(User.create("기존 사용자", "original@example.com"));
        Post originalPost = posts.save(Post.create("기존 제목", "기존 본문", originalUser));
        Comment originalComment = comments.save(Comment.create("기존 댓글", originalUser, originalPost));
        seeder.run(new DefaultApplicationArguments());
        assertThat(users.count()).isEqualTo(6);
        assertThat(posts.count()).isEqualTo(13);
        assertThat(comments.count()).isEqualTo(37);
        assertThat(users.findById(originalUser.getId()).orElseThrow().getUsername()).isEqualTo("기존 사용자");
        assertThat(posts.findById(originalPost.getId()).orElseThrow().getContent()).isEqualTo("기존 본문");
        assertThat(comments.findById(originalComment.getId()).orElseThrow().getContent()).isEqualTo("기존 댓글");
    }

    @Test
    @Transactional
    void leavesAnOlderFixedTextMockDatasetUntouched() {
        posts.deleteAll();
        users.deleteAll();
        users.flush();
        User originalUser = users.save(User.create("스프링러너", "mock-user-1@blog.example"));
        Post originalPost = posts.save(Post.create("[MOCK] 기존 고정 제목", "기존 목 본문", originalUser));
        Comment originalComment = comments.save(Comment.create("기존 목 댓글", originalUser, originalPost));
        seeder.run(new DefaultApplicationArguments());
        assertThat(users.count()).isEqualTo(1);
        assertThat(posts.count()).isEqualTo(1);
        assertThat(comments.count()).isEqualTo(1);
        assertThat(users.findById(originalUser.getId()).orElseThrow().getUsername()).isEqualTo("스프링러너");
        assertThat(posts.findById(originalPost.getId()).orElseThrow().getContent()).isEqualTo("기존 목 본문");
        assertThat(comments.findById(originalComment.getId()).orElseThrow().getContent()).isEqualTo("기존 목 댓글");
    }

}
