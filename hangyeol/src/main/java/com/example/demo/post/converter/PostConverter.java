package com.example.demo.post.converter;

import com.example.demo.post.dto.PostResDTO;
import com.example.demo.post.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class PostConverter {
    public PostResDTO.PostCreateResponse toPostCreateResponse(Post post) {
        return new PostResDTO.PostCreateResponse(
                post.getId()
        );
    }

    public PostResDTO.PostGetResponse toPostGetResponse(Post post){
        return new PostResDTO.PostGetResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getId(),
                post.getIsPrivate(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getDeletedAt()
        );
    }

    public PostResDTO.PostUpdateResponse toPostUpdateResponse(Post post){
        return new PostResDTO.PostUpdateResponse(
                post.getId(),
                post.getTitle(),
                post.getContent()
        );
    }

    public PostResDTO.PostDeleteResponse toPostDeleteResponse(Long postId){
        return new PostResDTO.PostDeleteResponse(
                postId
        );
    }

    public PostResDTO.PostGetListResponse toPostGetListResponse(List<Post> posts) {
        List<PostResDTO.PostGetResponse> postList = posts.stream()
                .map(this::toPostGetResponse)
                .toList();

        return new PostResDTO.PostGetListResponse(postList);
    }
}
