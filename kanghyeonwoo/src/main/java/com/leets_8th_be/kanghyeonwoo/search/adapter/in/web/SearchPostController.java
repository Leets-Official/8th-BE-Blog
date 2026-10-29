package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import com.leets_8th_be.kanghyeonwoo.shared.web.PageResponse;
import com.leets_8th_be.kanghyeonwoo.search.adapter.in.web.dto.PostSummaryResponse;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchPostPage;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.RecordSearchUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.SearchPostsUseCase;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.SearchSession;
import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_V2 + "/posts")
public class SearchPostController {
    private final SearchPostsUseCase searchPosts;
    private final RecordSearchUseCase recordSearch;

    @GetMapping
    @Operation(summary = "게시글 목록 조회 (검색 및 페이징)", description = "활성 게시글을 최신순으로 조회하고, 선택적으로 제목을 검색합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "페이지, 크기 또는 검색어 형식 오류",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(ref = "#/components/schemas/ApiResponseVoid"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "405", ref = "#/components/responses/MethodNotAllowed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerError")
    })
    public ResponseEntity<ApiResponse<PageResponse<PostSummaryResponse>>> search(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "제목 검색 키워드 (선택)", example = "Spring")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(hidden = true) SearchSession session) {
        SearchPostPage result = searchPosts.search(page, size, keyword);
        if (page == 1 && !SearchNormalization.normalize(keyword).isEmpty()) {
            String normalizedKeyword = SearchNormalization.requireKeyword(keyword);
            recordSearch.record(normalizedKeyword, session.key(), !result.items().isEmpty());
        }
        PageResponse<PostSummaryResponse> response = new PageResponse<>(result.items().stream()
                .map(post -> new PostSummaryResponse(post.id(), post.title(), post.authorId(), post.createdAt())).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "게시글 목록 조회가 완료되었습니다.", response));
    }
}
