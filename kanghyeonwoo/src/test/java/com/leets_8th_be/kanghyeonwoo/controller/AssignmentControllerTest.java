package com.leets_8th_be.kanghyeonwoo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /health 및 /api/v1/health 요청 시 ApiResponse envelope으로 감싼 ok 응답을 반환한다")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("ok"));

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("ok"));
    }

    @Test
    @DisplayName("POST /string/repeat 정상 요청 시 ApiResponse envelope 및 복제된 문자열 data를 반환한다")
    void repeatStringSuccess() throws Exception {
        String requestJson = "{\"value\":\"hello\"}";

        mockMvc.perform(post("/string/repeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("문자열 복제에 성공하였습니다."))
                .andExpect(jsonPath("$.data.string_one").value("hello"))
                .andExpect(jsonPath("$.data.string_two").value("hello"));

        mockMvc.perform(post("/api/v1/string/repeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.string_one").value("hello"))
                .andExpect(jsonPath("$.data.string_two").value("hello"));
    }

    @Test
    @DisplayName("POST /string/repeat 요청 시 빈 문자열 또는 공백이면 400 Bad Request와 실패 ApiResponse를 반환한다")
    void repeatInvalidInput() throws Exception {
        String emptyJson = "{\"value\":\"\"}";
        mockMvc.perform(post("/string/repeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").isEmpty());

        String blankJson = "{\"value\":\"   \"}";
        mockMvc.perform(post("/string/repeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("POST /string/repeat 요청 시 본문이 누락되면 400 Bad Request와 실패 ApiResponse를 반환한다")
    void repeatMissingBody() throws Exception {
        mockMvc.perform(post("/string/repeat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
