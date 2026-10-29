package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeleteCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HideCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.HidePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.user.application.model.UserProfile;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.CreateUserProfileUseCase;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileEmailLookup;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("mock-data")
@Order(30)
@RequiredArgsConstructor
@Slf4j
public class MockDataSeeder implements ApplicationRunner {
    private static final long TEXT_SEED = 20261026L;
    private final UserProfileEmailLookup users;
    private final CreateUserProfileUseCase createUsers;
    private final CreatePostUseCase createPosts;
    private final HidePostUseCase hidePosts;
    private final CreateCommentUseCase createComments;
    private final HideCommentUseCase hideComments;
    private final DeleteCommentUseCase deleteComments;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByEmail("mock-user-1@blog.example").isPresent()) {
            log.info("목 데이터가 이미 존재하여 추가하지 않습니다.");
            return;
        }
        var generator = new MockDataGenerator(TEXT_SEED);
        var authors = new ArrayList<UserProfile>();
        for (int i = 0; i < 5; i++) {
            UserProfile user = createUsers.create(generator.userName(), "mock-user-" + (i + 1) + "@blog.example");
            authors.add(user);
            log.info("목 사용자: userId={}, username={}, email={}", user.id().value(), user.username(), user.email());
        }
        for (int i = 0; i < 12; i++) {
            var post = createPosts.create(new CreatePostCommand(generator.postTitle(), generator.postContent(),
                    authors.get(i % authors.size()).id()));
            var postId = new PostId(post.id().value());
            var parent = createComments.create(new CreateCommentCommand(postId,
                    authors.get((i + 1) % authors.size()).id(), null, "질문: " + generator.commentContent()));
            if (i == 0 || i == 2) hideComments.hide(parent.comment().id());
            var reply = createComments.create(new CreateCommentCommand(postId,
                    authors.get((i + 2) % authors.size()).id(), parent.comment().id(), "답변: " + generator.commentContent()));
            var additional = createComments.create(new CreateCommentCommand(postId,
                    authors.get((i + 3) % authors.size()).id(), null, "의견: " + generator.commentContent()));
            if (i == 1 || i == 3) deleteComments.delete(additional.comment().id());
            if (i >= 10) hidePosts.hide(postId);
            log.info("목 게시글: postId={}, status={}, parentCommentId={}, replyId={}",
                    postId.value(), i >= 10 ? "HIDDEN" : "ACTIVE", parent.comment().id().value(),
                    reply.comment().id().value());
        }
        log.info("목 데이터 생성: 사용자 5명, 게시글 12개(숨김 2개), 댓글 36개(대댓글 12개, 숨김 2개, 삭제 2개)");
    }
}
