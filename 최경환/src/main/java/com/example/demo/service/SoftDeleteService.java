package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SoftDeleteService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public SoftDeleteService(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // 사용자와 사용자 소유 게시글·댓글을 함께 Soft Delete
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        List<Post> posts = postRepository.findAllByUser(user);
        List<Comment> comments = new ArrayList<>(commentRepository.findAllByUser(user));

        for (Post post : posts) {
            post.markDeleted();
            comments.addAll(commentRepository.findAllByPost(post));
        }

        for (Comment comment : comments) {
            comment.markDeleted();
        }

        user.markDeleted();

        commentRepository.saveAll(comments);
        postRepository.saveAll(posts);
        userRepository.save(user);
    }

    // 작성자 권한을 확인하고 게시글과 댓글을 Soft Delete
    public void deletePost(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글이 존재하지 않거나 삭제되었습니다."
                ));

        if (!post.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 작성자만 삭제할 수 있습니다.");
        }

        List<Comment> comments = commentRepository.findAllByPost(post);
        for (Comment comment : comments) {
            comment.markDeleted();
        }

        post.markDeleted();

        commentRepository.saveAll(comments);
        postRepository.save(post);
    }
}
