package com.leets_8th_be.kanghyeonwoo.auth;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.assertj.core.api.Assertions.*;
class KakaoClientTest {
 @Test void exchangesAuthorizationCodeAndFetchesVerifiedIdentity() {
  var builder=RestClient.builder();var server=MockRestServiceServer.bindTo(builder).build();
  var client=new KakaoClient("app-id","app-secret","https://api.example/callback","https://web.example/callback",builder.build());
  server.expect(requestTo("https://kauth.kakao.com/oauth/token")).andExpect(method(org.springframework.http.HttpMethod.POST)).andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=authorization_code"))).andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=app-secret"))).andRespond(withSuccess("{\"access_token\":\"kakao-access\"}",MediaType.APPLICATION_JSON));
  server.expect(requestTo("https://kapi.kakao.com/v2/user/me")).andExpect(header("Authorization","Bearer kakao-access")).andRespond(withSuccess("{\"id\":12345,\"kakao_account\":{\"email\":\"USER@EXAMPLE.COM\",\"is_email_valid\":true,\"is_email_verified\":true}}",MediaType.APPLICATION_JSON));
  assertThat(client.identity("authorization-code")).isEqualTo(new KakaoClient.Identity("12345","user@example.com"));server.verify();
 }
}
