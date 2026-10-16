package com.leets_8th_be.kanghyeonwoo.dto;

import com.leets_8th_be.kanghyeonwoo.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {

    private Long id;
    private String title;
    private Long authorId;
    private LocalDateTime createdAt;

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getId(),
                post.getCreatedAt()
        );
    }
}
