package org.example._thbe.domain.post.dto;

import org.example._thbe.domain.post.entity.Post;

import java.time.LocalDate;
import java.util.List;

public record PostListResponse(List<PostListDetail> posts) {

    public static PostListResponse from(List<Post> posts) {
        return new PostListResponse(posts.stream()
                .map(p -> PostListDetail.from(p.getId(),
                        p.getTitle(),
                        p.getContent(),
                        p.getMember().getNickname(),
                        p.getCreatedAt().toLocalDate()))
                .toList());
    }

    public record PostListDetail(Long postId,String title,String content,String writer,LocalDate createdDate) {
        public static PostListDetail from(Long postId, String title, String content, String writer, LocalDate createdDate) {
            return new PostListDetail(postId,title,content,writer,createdDate);
        }

    }
}
