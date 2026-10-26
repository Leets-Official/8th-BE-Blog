package com.leets_8th_be.kanghyeonwoo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "게시글 삭제 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDeleteResponse {

    @Schema(description = "삭제된 게시글 ID", example = "1")
    private Long deletedPostId;
}
