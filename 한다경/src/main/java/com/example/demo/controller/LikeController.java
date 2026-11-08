package com.example.demo.controller;

import com.example.demo.controller.docs.LikeControllerDocs;

import com.example.demo.dto.response.LikeResponse;
import com.example.demo.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class LikeController implements LikeControllerDocs {
    private final LikeService likes;

    @Override
    @PostMapping("/posts/{postId}/likes")
    public LikeResponse likePost(@PathVariable Long postId, Principal principal) {
        return likes.likePost(postId, Long.valueOf(principal.getName()));
    }

    @Override
    @DeleteMapping("/posts/{postId}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId, Principal principal) {
        likes.unlikePost(postId, Long.valueOf(principal.getName()));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/comments/{commentId}/likes")
    public LikeResponse likeComment(@PathVariable Long commentId, Principal principal) {
        return likes.likeComment(commentId, Long.valueOf(principal.getName()));
    }

    @Override
    @DeleteMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> unlikeComment(@PathVariable Long commentId, Principal principal) {
        likes.unlikeComment(commentId, Long.valueOf(principal.getName()));
        return ResponseEntity.noContent().build();
    }
}
