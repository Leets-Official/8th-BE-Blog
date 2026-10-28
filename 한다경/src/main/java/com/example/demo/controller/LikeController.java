package com.example.demo.controller;

import com.example.demo.dto.response.LikeResponse;
import com.example.demo.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likes;

    @PostMapping("/posts/{postId}/likes")
    public LikeResponse likePost(@PathVariable Long postId, @RequestHeader("X-User-Id") Long userId) {
        return likes.likePost(postId, userId);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, @RequestHeader("X-User-Id") Long userId) {
        likes.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/likes")
    public LikeResponse likeComment(@PathVariable Long commentId, @RequestHeader("X-User-Id") Long userId) {
        return likes.likeComment(commentId, userId);
    }

    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId, @RequestHeader("X-User-Id") Long userId) {
        likes.unlikeComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
