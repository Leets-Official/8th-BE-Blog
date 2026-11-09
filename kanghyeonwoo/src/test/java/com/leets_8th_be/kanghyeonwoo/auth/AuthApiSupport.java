package com.leets_8th_be.kanghyeonwoo.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@org.springframework.test.annotation.DirtiesContext(
        classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
abstract class AuthApiSupport {
    @org.springframework.beans.factory.annotation.Autowired
    protected org.springframework.test.web.servlet.MockMvc mvc;

    @org.springframework.beans.factory.annotation.Autowired
    protected com.fasterxml.jackson.databind.ObjectMapper json;

    protected com.fasterxml.jackson.databind.JsonNode signupAndLogin(String name) throws Exception {
        String body =
                json.writeValueAsString(
                        java.util.Map.of(
                                "email",
                                name + "@example.com",
                                "password",
                                "password123",
                                "nickname",
                                name));
        mvc.perform(post("/api/v2/auth/signup").contentType("application/json").content(body))
                .andExpect(status().isCreated());
        var result =
                mvc.perform(
                                post("/api/v2/auth/login")
                                        .contentType("application/json")
                                        .content(body))
                        .andExpect(status().isOk())
                        .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("data");
    }
}
