package org.example._thbe.domain.post.dto;

import org.example._thbe.domain.post.entity.Post;

import java.time.LocalDate;
import java.util.List;

public record PostListResponse(List<PostListDetail> posts) {

    public static PostListResponse from(List<Post> posts) {
        return new PostListResponse(posts.stream()
                .map( PostListDetail::from)
                .toList());
    }

    public record PostListDetail(Long postId,String title,String content,String writer,LocalDate createdDate) {
        public static PostListDetail from(Post p) {
            return new PostListDetail(p.getId(),p.getTitle(),p.getContent(),p.getMember().getNickname(),p.getCreatedAt().toLocalDate());
        }

    }
}
