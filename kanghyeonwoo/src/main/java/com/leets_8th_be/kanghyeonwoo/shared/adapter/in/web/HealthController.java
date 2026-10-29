package com.leets_8th_be.kanghyeonwoo.shared.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.shared.web.ApiPaths;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assignment", description = "기본 과제 및 헬스체크 API")
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.API_V1)
public class HealthController {


    @Operation(summary = "서버 헬스체크", description = "서버 정상 구동 상태를 확인합니다.")
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("ok"));
    }

}
