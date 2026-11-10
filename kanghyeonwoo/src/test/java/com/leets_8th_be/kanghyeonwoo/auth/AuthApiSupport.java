package com.leets_8th_be.kanghyeonwoo.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AuthApiSupport {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    protected JsonNode signupAndLogin(String name) throws Exception {
        String body = json.writeValueAsString(
                Map.of("email", name + "@example.com", "password", "password123", "nickname", name));
        mvc.perform(post("/api/v2/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        var result = mvc.perform(post("/api/v2/auth/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
