package com.leets_8th_be.kanghyeonwoo.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.blog.application.model.PostView;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeletePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.UpdatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.PostStatus;
import com.leets_8th_be.kanghyeonwoo.entity.Report;
import com.leets_8th_be.kanghyeonwoo.entity.ReportTargetType;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.ReportRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import com.leets_8th_be.kanghyeonwoo.shared.domain.UserId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@WithMockUser(username = "1", roles = "ADMIN")
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
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private HidePostUseCase hidePost;

    @Autowired
    private CreatePostUseCase createPost;

    @Autowired
    private UpdatePostUseCase updatePost;

    @Autowired
    private DeletePostUseCase deletePost;

    @PersistenceContext
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(
                User.builder().username("테스터").email("tester@example.com").build());
        TestSecurityContextHolder.setAuthentication(new UsernamePasswordAuthenticationToken(
                testUser.getId().toString(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    @DisplayName("필수 요청 본문이 없으면 400과 MISSING_REQUEST_BODY 및 null 데이터를 반환한다.")
    void createPostMissingBody() throws Exception {
        mockMvc.perform(post("/api/v2/posts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("MISSING_REQUEST_BODY"))
                .andExpect(jsonPath("$.message").value("전송된 요청 본문 내용이 없습니다."))
                .andExpect(jsonPath("$").value(hasKey("data")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("잘못된 JSON은 본문 누락 대신 INVALID_INPUT_VALUE와 null 데이터를 반환한다.")
    void createPostMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v2/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$").value(hasKey("data")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("본문 필드 타입 오류는 INVALID_INPUT_VALUE와 null 데이터를 반환한다.")
    void createPostInvalidBodyFieldType() throws Exception {
        mockMvc.perform(post("/api/v2/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                        {"title":{},"content":"본문"}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$").value(hasKey("data")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("신규 게시글을 정상적으로 등록하면 201 Created와 생성된 게시글 데이터를 반환한다.")
    void createPostSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "테스트 제목");
        request.put("content", "테스트 본문 내용입니다.");
        request.put("authorId", testUser.getId());

        mockMvc.perform(post("/api/v2/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("RESOURCE_CREATED"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 본문 내용입니다."))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("제목이 누락되거나 빈 문자열이면 400과 INVALID_INPUT_VALUE를 반환한다.")
    void createPostInvalidTitle() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "   ");
        request.put("content", "본문 내용");
        request.put("authorId", testUser.getId());

        mockMvc.perform(post("/api/v2/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("제목은 필수 입력 항목입니다."))
                .andExpect(jsonPath("$").value(hasKey("data")))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 authorId로 등록을 시도하면 404와 USER_NOT_FOUND를 반환한다.")
    void createPostUserNotFound() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("title", "제목");
        request.put("content", "본문");
        request.put("authorId", 999999L);

        mockMvc.perform(
                        post("/api/v2/posts")
                                .with(org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors.user("999999"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 시 200 OK와 상세 정보(updatedAt 포함)를 반환한다.")
    void getPostDetailSuccess() throws Exception {
        Post post = postRepository.save(Post.create("상세 제목", "상세 본문", testUser));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/" + post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("상세 제목"))
                .andExpect(jsonPath("$.data.content").value("상세 본문"))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 ID로 상세 조회 시 404와 POST_NOT_FOUND를 반환한다.")
    void getPostDetailNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 ID의 게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("블로그 공개 숨김 포트로 숨긴 게시글은 상세 조회에서 제외된다.")
    void hiddenPostIsNotAvailableFromDetailEndpoint() throws Exception {
        Post post = postRepository.save(Post.create("숨길 글", "본문", testUser));

        hidePost.hide(new PostId(post.getId()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/" + post.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    @DisplayName("게시글 ID에 문자가 전달되면 400과 TYPE_MISMATCH를 반환한다.")
    void getPostDetailTypeMismatch() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/invalid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"));
    }

    @Test
    @DisplayName("게시글 목록 조회 시 200 OK와 최신순 정렬된 요약 목록과 페이징 정보를 반환한다.")
    void getPostsSuccess() throws Exception {
        createPost("첫 번째 글", "본문 1");
        createPost("두 번째 글", "본문 2");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].title").value("두 번째 글"))
                .andExpect(jsonPath("$.data.items[1].title").value("첫 번째 글"));
    }

    @Test
    @DisplayName("키워드로 게시글 목록을 검색하면 일치하는 게시글만 반환한다.")
    void searchPostsByKeyword() throws Exception {
        createPost("Spring REST API", "RESTful 설계");
        createPost("JPA Study", "엔티티 매핑");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("keyword", "REST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("Spring REST API"));
    }

    @Test
    @DisplayName("작성자 본인이 게시글 수정을 요청하면 200 OK와 수정된 데이터를 반환한다.")
    void updatePostSuccess() throws Exception {
        Post post = postRepository.save(Post.create("원래 제목", "원래 본문", testUser));

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", testUser.getId());
        updateRequest.put("title", "수정된 제목");
        updateRequest.put("content", "수정된 본문");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 본문"))
                .andExpect(jsonPath("$.data.authorId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());
    }

    @Test
    @DisplayName("타인이 작성한 게시글 수정을 시도하면 403과 POST_ACCESS_DENIED를 반환한다.")
    void updatePostForbidden() throws Exception {
        Post post = postRepository.save(Post.create("원래 제목", "원래 본문", testUser));

        User anotherUser = userRepository.save(
                User.builder().username("타인").email("other@example.com").build());

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", anotherUser.getId());
        updateRequest.put("title", "해킹 시도");
        updateRequest.put("content", "내용 변경 시도");

        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v2/posts/" + post.getId())
                                .with(org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors.user(
                                        anotherUser.getId().toString()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("작성자 본인만 수정 및 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정을 시도하면 404와 POST_NOT_FOUND를 반환한다.")
    void updatePostNotFound() throws Exception {
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", testUser.getId());
        updateRequest.put("title", "수정 제목");
        updateRequest.put("content", "수정 본문");

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/posts/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    @DisplayName("작성자 본인이 게시글 삭제를 요청하면 200 OK와 삭제된 ID를 반환한다.")
    void deletePostSuccess() throws Exception {
        Post post = postRepository.save(Post.create("삭제할 글", "삭제할 본문", testUser));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("게시글이 성공적으로 삭제되었습니다."))
                .andExpect(jsonPath("$.data.deletedPostId").value(post.getId()));

        entityManager.flush();
        entityManager.clear();
        Assertions.assertThat(
                        postRepository.findById(post.getId()).orElseThrow().getStatus())
                .isEqualTo(PostStatus.DELETED);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/" + post.getId()))
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("keyword", "삭제할 글"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("authorId", testUser.getId());
        updateRequest.put("title", "삭제 후 수정 시도");
        updateRequest.put("content", "삭제 후 본문 수정 시도");
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts/" + post.getId() + "/comments"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("타인이 작성한 게시글 삭제를 시도하면 403과 POST_ACCESS_DENIED를 반환한다.")
    void deletePostForbidden() throws Exception {
        Post post = postRepository.save(Post.create("삭제할 글", "삭제할 본문", testUser));

        User anotherUser = userRepository.save(
                User.builder().username("타인2").email("other2@example.com").build());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v2/posts/" + post.getId())
                                .with(org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors.user(
                                        anotherUser.getId().toString())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("작성자 본인만 수정 및 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제를 시도하면 404와 POST_NOT_FOUND를 반환한다.")
    void deletePostNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/posts/999999")
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    @DisplayName("신고(Report)가 달린 게시글을 삭제해도 외래키 에러 없이 정상 삭제되고 신고 이력은 보존된다.")
    void deletePostWithReportIntegritySuccess() throws Exception {
        // given: 게시글 등록 및 해당 게시글에 대한 신고 접수
        Post post = postRepository.save(Post.create("신고 대상 글", "유해 콘텐츠", testUser));

        User reporter = userRepository.save(
                User.builder().username("신고자").email("reporter@test.com").build());
        reportRepository.save(Report.ofPost(reporter, post, "부적절한 게시글 신고"));

        // when: 게시글 삭제 요청
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.deletedPostId").value(post.getId()));

        // then: 게시글은 DELETED 상태로 보존되고 신고 내역은 스냅샷과 함께 안전하게 보존됨
        entityManager.flush();
        entityManager.clear();
        Assertions.assertThat(
                        postRepository.findById(post.getId()).orElseThrow().getStatus())
                .isEqualTo(PostStatus.DELETED);

        List<Report> reports = reportRepository.findByTargetTypeAndTargetId(ReportTargetType.POST, post.getId());
        Assertions.assertThat(reports).hasSize(1);
        Assertions.assertThat(reports.get(0).getTargetTitleSnapshot()).isEqualTo("신고 대상 글");
        Assertions.assertThat(reports.get(0).getTargetContentSnapshot()).isEqualTo("유해 콘텐츠");
        Assertions.assertThat(reports.get(0).getReason()).isEqualTo("부적절한 게시글 신고");
    }

    @Test
    @DisplayName("신고가 달린 댓글이 포함된 게시글 삭제 시 게시글과 댓글은 보존되고 댓글 신고 내역도 보존된다.")
    void deletePostWithCommentReportIntegritySuccess() throws Exception {
        Post post = Post.create("게시글", "본문", testUser);
        Comment comment = Comment.create("신고 대상 욕설 댓글", testUser, post);
        post.addComment(comment);
        post = postRepository.save(post);
        Long commentId = post.getComments().get(0).getId();

        User reporter = userRepository.save(
                User.builder().username("신고자2").email("reporter2@test.com").build());
        reportRepository.save(Report.ofComment(reporter, post.getComments().get(0), "욕설 신고"));

        // when: 게시글 삭제 요청
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/posts/" + post.getId())
                        .param("authorId", String.valueOf(testUser.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then: 게시글은 DELETED 상태가 되고 댓글과 신고 이력은 보존됨
        entityManager.flush();
        entityManager.clear();
        Assertions.assertThat(
                        postRepository.findById(post.getId()).orElseThrow().getStatus())
                .isEqualTo(PostStatus.DELETED);
        Assertions.assertThat(commentRepository.findById(commentId)).isPresent();

        List<Report> commentReports = reportRepository.findByTargetTypeAndTargetId(ReportTargetType.COMMENT, commentId);
        Assertions.assertThat(commentReports).hasSize(1);
        Assertions.assertThat(commentReports.get(0).getTargetContentSnapshot()).isEqualTo("신고 대상 욕설 댓글");
    }

    @Test
    void paginatesSearchResultsWithTotalCountsAndRejectsInvalidPages() throws Exception {
        for (int i = 1; i <= 3; i++) {
            createPost("pagination " + i, "본문");
        }
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts")
                        .param("keyword", "pagination")
                        .param("page", "2")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].title").value("pagination 1"))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("keyword", "no-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("page", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchProjectionTracksPostUpdatesHidesAndDeletes() throws Exception {
        PostView post = createPost("projection original", "본문");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("keyword", "projection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1));

        updatePost.update(new UpdatePostCommand(post.id(), new UserId(testUser.getId()), "renamed title", "본문 수정"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts").param("keyword", "projection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));

        hidePost.hide(post.id());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
        deletePost.delete(new DeletePostCommand(post.id(), new UserId(testUser.getId())));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    private PostView createPost(String title, String content) {
        return createPost.create(new CreatePostCommand(title, content, new UserId(testUser.getId())));
    }
}
