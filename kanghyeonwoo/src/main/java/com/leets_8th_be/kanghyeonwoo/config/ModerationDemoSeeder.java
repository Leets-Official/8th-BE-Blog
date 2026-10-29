package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreateCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.DeleteCommentUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostCommand;
import com.leets_8th_be.kanghyeonwoo.blog.application.port.in.CreatePostUseCase;
import com.leets_8th_be.kanghyeonwoo.blog.domain.model.PostId;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.CreateUserProfileUseCase;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.UserProfileEmailLookup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("moderation-demo")
@Order(30)
@RequiredArgsConstructor
@Slf4j
public class ModerationDemoSeeder implements ApplicationRunner {
    private static final String AUTHOR_EMAIL = "moderation-author@blog.example";
    private final UserProfileEmailLookup users;
    private final CreateUserProfileUseCase createUsers;
    private final CreatePostUseCase createPosts;
    private final CreateCommentUseCase createComments;
    private final DeleteCommentUseCase deleteComments;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByEmail(AUTHOR_EMAIL).isPresent()) return;
        var author = createUsers.create("신고 데모 작성자", AUTHOR_EMAIL);
        var reporter = createUsers.create("신고 데모 신고자", "moderation-reporter@blog.example");
        var processor = createUsers.create("신고 데모 처리자", "moderation-processor@blog.example");
        var post = createPosts.create(new CreatePostCommand("신고 데모 게시글",
                "신고 당시 본문을 스냅샷으로 보존합니다.", author.id()));
        var postId = new PostId(post.id().value());
        var parent = createComments.create(new CreateCommentCommand(postId, author.id(), null, "신고 데모 댓글 질문"));
        createComments.create(new CreateCommentCommand(postId, processor.id(), parent.comment().id(), "정상 답변입니다."));
        var deleted = createComments.create(new CreateCommentCommand(postId, author.id(), null, "삭제할 댓글"));
        deleteComments.delete(deleted.comment().id());
        log.info("신고 데모: authorId={}, reporterId={}, processorId={}, postId={}, commentId={}",
                author.id().value(), reporter.id().value(), processor.id().value(), post.id().value(),
                parent.comment().id().value());
    }
}
