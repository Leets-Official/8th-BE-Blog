package com.example.demo;

import com.example.demo.controller.PostController;
import com.example.demo.controller.ReportController;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.service.PostService;
import com.example.demo.service.ReportService;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OptimisticLockHttpTests {
    static Stream<RuntimeException> conflicts() {
        return Stream.of(new ObjectOptimisticLockingFailureException("Post", 1L),
                new OptimisticLockException("stale entity"));
    }

    @ParameterizedTest
    @MethodSource("conflicts")
    void postUpdateConflictReturns409(RuntimeException conflict) throws Exception {
        PostService service = mock(PostService.class);
        when(service.updatePost(eq(1L), eq(2L), any())).thenThrow(conflict);
        var mvc = MockMvcBuilders.standaloneSetup(new PostController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(put("/posts/1").header("X-User-Id", 2)
                        .contentType("application/json")
                        .content("{\"title\":\"title\",\"content\":\"content\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"))
                .andExpect(jsonPath("$.message").value(
                        "다른 요청으로 데이터가 변경되었습니다. 최신 상태를 확인한 후 다시 요청해주세요."));
    }

    @ParameterizedTest
    @MethodSource("conflicts")
    void approvalConflictReturns409(RuntimeException conflict) throws Exception {
        ReportService service = mock(ReportService.class);
        when(service.approve(1L, 2L)).thenThrow(conflict);
        var mvc = MockMvcBuilders.standaloneSetup(new ReportController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/reports/1/approve").header("X-User-Id", 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"));
    }
}
