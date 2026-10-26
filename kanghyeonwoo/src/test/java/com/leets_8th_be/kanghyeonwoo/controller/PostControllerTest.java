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

    @Autowired
    private com.leets_8th_be.kanghyeonwoo.repository.CommentRepository commentRepository;

    @Autowired
    private com.leets_8th_be.kanghyeonwoo.repository.ReportRepository reportRepository;

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

        mockMvc.perform(post("/api/v1/posts")
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

        mockMvc.perform(post("/api/v1/posts")
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

        mockMvc.perform(post("/api/v1/posts")
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

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts/" + post.getId()))
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
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4001"))
                .andExpect(jsonPath("$.message").value("해당 ID의 게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("게시글 ID에 문자가 전달되면 400 Bad Request와 COMMON400_TYPE을 반환한다.")
    void getPostDetailTypeMismatch() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("COMMON400_TYPE"));
    }

    @Test
    @DisplayName("게시글 목록 조회 시 200 OK와 최신순 정렬된 요약 목록과 페이징 정보를 반환한다.")
    void getPostsSuccess() throws Exception {
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("첫 번째 글", "본문 1", testUser));
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("두 번째 글", "본문 2", testUser));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].title").value("두 번째 글"))
                .andExpect(jsonPath("$.data.items[1].title").value("첫 번째 글"));
    }

    @Test
    @DisplayName("키워드로 게시글 목록을 검색하면 일치하는 게시글만 반환한다.")
    void searchPostsByKeyword() throws Exception {
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("Spring REST API", "RESTful 설계", testUser));
        postRepository.save(com.leets_8th_be.kanghyeonwoo.entity.Post.create("JPA Study", "엔티티 매핑", testUser));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("keyword", "REST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("Spring REST API"));
    }

    @Test
    @DisplayName("작성자 본인이 게시글 수정을 요청하면 200 OK와 수정된 데이터를 반환한다.")
    void updatePostSuccess() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("원래 제목", "원래 본문", testUser)
        );

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", testUser.getId());
        updateRequest.put("title", "수정된 제목");
        updateRequest.put("content", "수정된 본문");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 본문"))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("타인이 작성한 게시글 수정을 시도하면 403 Forbidden과 POST4003을 반환한다.")
    void updatePostForbidden() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("원래 제목", "원래 본문", testUser)
        );

        User anotherUser = userRepository.save(User.builder().username("타인").email("other@example.com").build());

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", anotherUser.getId());
        updateRequest.put("title", "해킹 시도");
        updateRequest.put("content", "내용 변경 시도");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4003"))
                .andExpect(jsonPath("$.message").value("작성자 본인만 수정 및 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정을 시도하면 404 Not Found와 POST4001을 반환한다.")
    void updatePostNotFound() throws Exception {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", testUser.getId());
        updateRequest.put("title", "수정 제목");
        updateRequest.put("content", "수정 본문");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/posts/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4001"));
    }

    @Test
    @DisplayName("작성자 본인이 게시글 삭제를 요청하면 200 OK와 삭제된 ID를 반환한다.")
    void deletePostSuccess() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("삭제할 글", "삭제할 본문", testUser)
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 삭제되었습니다."))
                .andExpect(jsonPath("$.data.deletedPostId").value(post.getId()));
    }

    @Test
    @DisplayName("타인이 작성한 게시글 삭제를 시도하면 403 Forbidden과 POST4003을 반환한다.")
    void deletePostForbidden() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("삭제할 글", "삭제할 본문", testUser)
        );

        User anotherUser = userRepository.save(User.builder().username("타인2").email("other2@example.com").build());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/posts/" + post.getId())
                        .param("authorId", String.valueOf(anotherUser.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4003"))
                .andExpect(jsonPath("$.message").value("작성자 본인만 수정 및 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제를 시도하면 404 Not Found와 POST4001을 반환한다.")
    void deletePostNotFound() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/posts/999999")
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST4001"));
    }

    @Test
    @DisplayName("신고(Report)가 달린 게시글을 삭제해도 외래키 에러 없이 정상 삭제되고 신고 이력은 보존된다.")
    void deletePostWithReportIntegritySuccess() throws Exception {
        // given: 게시글 등록 및 해당 게시글에 대한 신고 접수
        com.leets_8th_be.kanghyeonwoo.entity.Post post = postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("신고 대상 글", "유해 콘텐츠", testUser)
        );

        User reporter = userRepository.save(User.builder().username("신고자").email("reporter@test.com").build());
        com.leets_8th_be.kanghyeonwoo.entity.Report report = reportRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Report.ofPost(reporter, post, "부적절한 게시글 신고")
        );

        // when: 게시글 삭제 요청
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("S200"))
                .andExpect(jsonPath("$.data.deletedPostId").value(post.getId()));

        // then: 게시글은 DB에서 삭제되었지만, 신고 내역은 스냅샷과 함께 안전하게 보존됨
        org.assertj.core.api.Assertions.assertThat(postRepository.findById(post.getId())).isEmpty();

        java.util.List<com.leets_8th_be.kanghyeonwoo.entity.Report> reports =
                reportRepository.findByTargetTypeAndTargetId(com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType.POST, post.getId());
        org.assertj.core.api.Assertions.assertThat(reports).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(reports.get(0).getTargetTitleSnapshot()).isEqualTo("신고 대상 글");
        org.assertj.core.api.Assertions.assertThat(reports.get(0).getTargetContentSnapshot()).isEqualTo("유해 콘텐츠");
        org.assertj.core.api.Assertions.assertThat(reports.get(0).getReason()).isEqualTo("부적절한 게시글 신고");
    }

    @Test
    @DisplayName("신고가 달린 댓글이 포함된 게시글 삭제 시 게시글과 댓글은 삭제되고 댓글 신고 내역은 보존된다.")
    void deletePostWithCommentReportIntegritySuccess() throws Exception {
        com.leets_8th_be.kanghyeonwoo.entity.Post post = com.leets_8th_be.kanghyeonwoo.entity.Post.create("게시글", "본문", testUser);
        com.leets_8th_be.kanghyeonwoo.entity.Comment comment = com.leets_8th_be.kanghyeonwoo.entity.Comment.create("신고 대상 욕설 댓글", testUser, post);
        post.addComment(comment);
        post = postRepository.save(post);
        Long commentId = post.getComments().get(0).getId();

        User reporter = userRepository.save(User.builder().username("신고자2").email("reporter2@test.com").build());
        com.leets_8th_be.kanghyeonwoo.entity.Report report = reportRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Report.ofComment(reporter, post.getComments().get(0), "욕설 신고")
        );

        // when: 게시글 삭제 요청 (연쇄적으로 댓글까지 삭제됨)
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then: 게시글과 댓글은 삭제되었지만, 댓글 신고 이력은 보존됨
        org.assertj.core.api.Assertions.assertThat(postRepository.findById(post.getId())).isEmpty();
        org.assertj.core.api.Assertions.assertThat(commentRepository.findById(commentId)).isEmpty();

        java.util.List<com.leets_8th_be.kanghyeonwoo.entity.Report> commentReports =
                reportRepository.findByTargetTypeAndTargetId(com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType.COMMENT, commentId);
        org.assertj.core.api.Assertions.assertThat(commentReports).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(commentReports.get(0).getTargetContentSnapshot()).isEqualTo("신고 대상 욕설 댓글");
    }
    @Test
    void paginatesSearchResultsWithTotalCountsAndRejectsInvalidPages() throws Exception {
        for (int i = 1; i <= 3; i++) postRepository.save(
                com.leets_8th_be.kanghyeonwoo.entity.Post.create("pagination " + i, "본문", testUser));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("keyword", "pagination").param("page", "2").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("pagination 1"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("keyword", "no-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/posts")
                        .param("page", "0")).andExpect(status().isBadRequest());
    }

}
