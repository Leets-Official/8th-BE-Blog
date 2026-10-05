package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        List<Post> posts = postRepository.findAllByUserAndDeletedAtIsNull(user);
        List<Comment> comments = new ArrayList<>(commentRepository.findAllByUserAndDeletedAtIsNull(user));

        for (Post post : posts) {
            post.markDeleted();
            comments.addAll(commentRepository.findAllByPostAndDeletedAtIsNull(post));
        }

        for (Comment comment : comments) {
            comment.markDeleted();
        }

        user.markDeleted();

        commentRepository.saveAll(comments);
        postRepository.saveAll(posts);
        userRepository.save(user);
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        List<Comment> comments = commentRepository.findAllByPostAndDeletedAtIsNull(post);
        for (Comment comment : comments) {
            comment.markDeleted();
        }

        post.markDeleted();

        commentRepository.saveAll(comments);
        postRepository.save(post);
    }
}
