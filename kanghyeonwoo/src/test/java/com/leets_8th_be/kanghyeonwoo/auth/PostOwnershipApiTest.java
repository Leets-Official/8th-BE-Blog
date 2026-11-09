package com.leets_8th_be.kanghyeonwoo.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;

class PostOwnershipApiTest extends AuthApiSupport {
    @Test
    void forgedAuthorCannotEditAnotherUsersPost() throws Exception {
        var owner = signupAndLogin("owner");
        var attacker = signupAndLogin("attacker");
        var created =
                mvc.perform(
                                post("/api/v2/posts")
                                        .header(
                                                "Authorization",
                                                "Bearer " + owner.get("accessToken").asText())
                                        .contentType("application/json")
                                        .content(
                                                "{\"title\":\"original\",\"content\":\"original\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();
        var data = json.readTree(created.getResponse().getContentAsString()).get("data");
        long id = data.get("id").asLong();
        mvc.perform(
                        patch("/api/v2/posts/" + id)
                                .header(
                                        "Authorization",
                                        "Bearer " + attacker.get("accessToken").asText())
                                .contentType("application/json")
                                .content(
                                        "{\"authorId\":1,\"title\":\"hacked\",\"content\":\"hacked\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(
                        get("/api/v2/posts/" + id)
                                .header(
                                        "Authorization",
                                        "Bearer " + owner.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("original"));
    }
}
