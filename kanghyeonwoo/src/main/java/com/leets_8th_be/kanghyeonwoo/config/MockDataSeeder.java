package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.entity.Comment;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.CommentRepository;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
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
    private final UserRepository users;
    private final PostRepository posts;
    private final CommentRepository comments;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByEmail("mock-user-1@blog.example").isPresent()) {
            log.info("목 데이터가 이미 존재하여 추가하지 않습니다.");
            return;
        }
        var generator = new MockDataGenerator(TEXT_SEED);
        var authors = new ArrayList<User>();
        for (int i = 0; i < 5; i++) {
            User user = users.save(User.create(generator.userName(), "mock-user-" + (i + 1) + "@blog.example"));
            authors.add(user);
            log.info("목 사용자: userId={}, username={}, email={}", user.getId(), user.getUsername(), user.getEmail());
        }
        for (int i = 0; i < 12; i++) {
            Post post = Post.create(generator.postTitle(), generator.postContent(),
                    authors.get(i % authors.size()));
            if (i >= 10) post.hide();
            posts.save(post);
            Comment parent = Comment.create("질문: " + generator.commentContent(),
                    authors.get((i + 1) % authors.size()), post);
            if (i == 0 || i == 2) parent.hide();
            comments.save(parent);
            Comment reply = comments.save(Comment.builder()
                    .content("답변: " + generator.commentContent())
                    .user(authors.get((i + 2) % authors.size())).post(post).parent(parent).build());
            Comment additional = Comment.create("의견: " + generator.commentContent(),
                    authors.get((i + 3) % authors.size()), post);
            if (i == 1 || i == 3) additional.delete();
            comments.save(additional);
            log.info("목 게시글: postId={}, status={}, parentCommentId={}, replyId={}",
                    post.getId(), post.getStatus(), parent.getId(), reply.getId());
        }
        log.info("목 데이터 생성: 사용자 5명, 게시글 12개(숨김 2개), 댓글 36개(대댓글 12개, 숨김 2개, 삭제 2개)");
    }
}
