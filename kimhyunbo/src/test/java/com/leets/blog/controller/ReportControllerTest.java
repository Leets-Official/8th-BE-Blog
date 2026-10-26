package com.leets.blog.controller;

import com.leets.blog.entity.Comment;
import com.leets.blog.entity.Member;
import com.leets.blog.entity.Post;
import com.leets.blog.repository.CommentRepository;
import com.leets.blog.repository.MemberRepository;
import com.leets.blog.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportControllerTest {

    // data.sql로 미리 넣어둔 회원
    private static final long AUTHOR_ID = 101L;
    private static final long REPORTER_ID = 102L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 댓글 작성 API가 없으므로 게시글과 댓글은 저장소로 직접 만든다.
    private Post savePost() {
        Member author = memberRepository.findById(AUTHOR_ID).orElseThrow();
        return postRepository.save(Post.builder().member(author).title("신고 대상").content("본문").build());
    }

    private Comment saveComment(Post post) {
        Member author = memberRepository.findById(AUTHOR_ID).orElseThrow();
        return commentRepository.save(Comment.builder().post(post).member(author).content("댓글").build());
    }

    private ResultActions report(String url, Long targetId, Long reporterId) throws Exception {
        return mockMvc.perform(post(url, targetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"reporterId": %d, "reason": "부적절한 내용"}
                        """.formatted(reporterId)));
    }

    private long reportPostAndGetId(Long postId) throws Exception {
        String response = report("/posts/{postId}/reports", postId, REPORTER_ID)
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceAll(".*\"data\":\\{\"id\":(\\d+).*", "$1"));
    }

    @Test
    @DisplayName("게시글을 신고하면 201과 PENDING 상태의 신고를 반환한다")
    void reportPost() throws Exception {
        Post post = savePost();

        report("/posts/{postId}/reports", post.getId(), REPORTER_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("S201"))
                .andExpect(jsonPath("$.data.targetType").value("POST"))
                .andExpect(jsonPath("$.data.targetId").value(post.getId()))
                .andExpect(jsonPath("$.data.reporterId").value(REPORTER_ID))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("같은 회원이 같은 게시글을 다시 신고하면 409와 REPORT4091을 반환한다")
    void reportPostDuplicated() throws Exception {
        Post post = savePost();
        report("/posts/{postId}/reports", post.getId(), REPORTER_ID).andExpect(status().isCreated());

        report("/posts/{postId}/reports", post.getId(), REPORTER_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT4091"));
    }

    @Test
    @DisplayName("다른 회원은 같은 게시글을 신고할 수 있다")
    void reportPostByAnotherMember() throws Exception {
        Post post = savePost();
        report("/posts/{postId}/reports", post.getId(), REPORTER_ID).andExpect(status().isCreated());

        report("/posts/{postId}/reports", post.getId(), AUTHOR_ID)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 신고하면 404와 POST4001을 반환한다")
    void reportPostNotFound() throws Exception {
        report("/posts/{postId}/reports", 999L, REPORTER_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST4001"));
    }

    @Test
    @DisplayName("신고 사유가 비어 있으면 400과 COMMON400_VALIDATION을 반환한다")
    void reportWithBlankReason() throws Exception {
        Post post = savePost();

        mockMvc.perform(post("/posts/{postId}/reports", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reporterId": 102, "reason": " "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON400_VALIDATION"))
                .andExpect(jsonPath("$.data[0].field").value("reason"));
    }

    @Test
    @DisplayName("댓글을 신고하면 201을 반환하고, 같은 회원이 다시 신고하면 409를 반환한다")
    void reportComment() throws Exception {
        Comment comment = saveComment(savePost());

        report("/comments/{commentId}/reports", comment.getId(), REPORTER_ID)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetType").value("COMMENT"))
                .andExpect(jsonPath("$.data.targetId").value(comment.getId()));

        report("/comments/{commentId}/reports", comment.getId(), REPORTER_ID)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT4091"));
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 신고하면 404와 COMMENT4001을 반환한다")
    void reportCommentNotFound() throws Exception {
        report("/comments/{commentId}/reports", 999L, REPORTER_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("COMMENT4001"));
    }

    @Test
    @DisplayName("신고를 처리하면 상태가 RESOLVED가 되고, 다시 처리하면 409와 REPORT4092를 반환한다")
    void resolveReport() throws Exception {
        long reportId = reportPostAndGetId(savePost().getId());

        mockMvc.perform(patch("/reports/{reportId}/resolve", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(patch("/reports/{reportId}/resolve", reportId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT4092"));
    }

    @Test
    @DisplayName("존재하지 않는 신고를 처리하면 404와 REPORT4001을 반환한다")
    void resolveReportNotFound() throws Exception {
        mockMvc.perform(patch("/reports/{reportId}/resolve", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT4001"));
    }
}
