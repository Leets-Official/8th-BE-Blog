package com.example.demo.post.dto;

import com.example.demo.global.enums.IsPrivate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PostReqDTO {
    public record PostCreateRequest(
            @NotNull(message = "작성자 ID는 필수입니다.")
            Long memberId,

            @NotBlank(message = "제목은 필수 입력 항목입니다.")
            @Size(max = 50, message = "게시글 제목은 50자를 초과할 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수 입력 항목입니다.")
            String content,

            IsPrivate isPrivate
    ) {
    }

    public record PostUpdateRequest(

            Long memberId,

            @NotBlank(message = "제목은 필수 입력 항목입니다.")
            @Size(max = 50, message = "게시글 제목은 50자를 초과할 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수 입력 항목입니다.")
            String content
    ){}

    public record PostDeletePost(
            Long memberId
    ){}
}
