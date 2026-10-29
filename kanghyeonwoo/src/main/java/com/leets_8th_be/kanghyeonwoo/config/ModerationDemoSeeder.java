package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
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
    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByEmail("moderation-author@blog.example").isPresent()) return;
        User author = users.save(User.create("신고 데모 작성자", "moderation-author@blog.example"));
        User reporter = users.save(User.create("신고 데모 신고자", "moderation-reporter@blog.example"));
        User processor = users.save(User.create("신고 데모 처리자", "moderation-processor@blog.example"));
        Post post = posts.save(Post.create("신고 데모 게시글", "신고 당시 본문을 스냅샷으로 보존합니다.", author));
        Comment parent = comments.save(Comment.create("신고 데모 댓글 질문", author, post));
        comments.save(Comment.builder().content("정상 답변입니다.").user(processor).post(post).parent(parent).build());
        Comment deleted = Comment.create("삭제할 댓글", author, post);
        deleted.delete();
        comments.save(deleted);
        log.info("신고 데모: authorId={}, reporterId={}, processorId={}, postId={}, commentId={}",
                author.getId(), reporter.getId(), processor.getId(), post.getId(), parent.getId());
    }
}
