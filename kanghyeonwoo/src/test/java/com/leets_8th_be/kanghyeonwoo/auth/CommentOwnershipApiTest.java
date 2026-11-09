package com.leets_8th_be.kanghyeonwoo.auth;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
class CommentOwnershipApiTest extends AuthApiSupport {
    @Test void commentOwnerCanEditButOtherUserCannotDelete() throws Exception {
        var owner=signupAndLogin("comment-owner");var attacker=signupAndLogin("comment-attacker");
        String bearer="Bearer "+owner.get("accessToken").asText();
        var post=mvc.perform(post("/api/v2/posts").header("Authorization",bearer).contentType("application/json").content("{\"title\":\"comment post\",\"content\":\"content\"}")).andExpect(status().isCreated()).andReturn();
        long id=json.readTree(post.getResponse().getContentAsString()).path("data").path("id").asLong();
        var comment=mvc.perform(post("/api/v2/posts/"+id+"/comments").header("Authorization",bearer).contentType("application/json").content("{\"content\":\"original comment\"}")).andExpect(status().isCreated()).andReturn();
        long commentId=json.readTree(comment.getResponse().getContentAsString()).path("data").path("id").asLong();
        mvc.perform(patch("/api/v2/comments/"+commentId).header("Authorization",bearer).contentType("application/json").content("{\"content\":\"edited comment\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v2/comments/"+commentId).header("Authorization","Bearer "+attacker.get("accessToken").asText())).andExpect(status().isForbidden());
        mvc.perform(get("/api/v2/posts/"+id+"/comments").header("Authorization",bearer)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].content").value("edited comment"));
        mvc.perform(delete("/api/v2/comments/"+commentId).header("Authorization",bearer)).andExpect(status().isOk());
    }
}
