package com.example.demo.post.dto;

import com.example.demo.global.enums.IsPrivate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PostReqDTO {
    @Schema(description = "게시글 작성 요청")
    public record PostCreateRequest(
            @Schema(description = "작성자 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "작성자 ID는 필수입니다.")
            Long memberId,

            @Schema(description = "게시글 제목 (50자 이하)", example = "첫 번째 게시글", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "제목은 필수 입력 항목입니다.")
            @Size(max = 50, message = "게시글 제목은 50자를 초과할 수 없습니다.")
            String title,

            @Schema(description = "게시글 내용", example = "안녕하세요. 첫 글입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "내용은 필수 입력 항목입니다.")
            String content,

            @Schema(description = "공개 여부", example = "PUBLIC")
            IsPrivate isPrivate
    ) {
    }

    @Schema(description = "게시글 수정 요청")
    public record PostUpdateRequest(

            @Schema(description = "요청한 사용자 ID (작성자 본인 확인용)", example = "1")
            Long memberId,

            @Schema(description = "수정할 제목 (50자 이하)", example = "수정된 제목", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "제목은 필수 입력 항목입니다.")
            @Size(max = 50, message = "게시글 제목은 50자를 초과할 수 없습니다.")
            String title,

            @Schema(description = "수정할 내용", example = "수정된 내용입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "내용은 필수 입력 항목입니다.")
            String content
    ){}

    @Schema(description = "게시글 삭제 요청")
    public record PostDeletePost(
            @Schema(description = "요청한 사용자 ID (작성자 본인 확인용)", example = "1")
            Long memberId
    ){}
}
