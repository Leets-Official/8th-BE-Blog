package com.leets_8th_be.kanghyeonwoo.auth;
import com.leets_8th_be.kanghyeonwoo.auth.application.*;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.*;
import org.springframework.context.annotation.*;
@Configuration
public class KakaoAdapters {
 @Bean Tickets authenticationTickets(OAuthTickets repository) {return new Tickets() {
  public java.util.Optional<Ticket> lock(String hash) {return repository.lock(hash).map(row->{var t=new Ticket(row.hash,row.kind,row.browserHash,row.expiresAt);t.kakaoId=row.kakaoId;t.email=row.email;t.userId=row.userId;return t;});}
  public void save(Ticket t) {var row=repository.findById(t.hash).orElseGet(()->new OAuthTicket(t.hash,t.kind,t.browserHash,t.expiresAt));row.kakaoId=t.kakaoId;row.email=t.email;row.userId=t.userId;repository.save(row);}
  public void delete(Ticket t) {repository.deleteById(t.hash);repository.flush();}
 };}
 @Bean Kakao authenticationKakao(KakaoClient client) {return new Kakao() {
  public String authorize(String state) {return client.authorize(state);}public String frontend(String code) {return client.frontend(code);}
  public Identity identity(String code) {var identity=client.identity(code);return new Identity(identity.id(),identity.email());}
 };}
 @Bean KakaoAuthenticationService kakaoAuthenticationService(Tickets tickets,Accounts accounts,AuthenticationService auth,Kakao kakao) {return new KakaoAuthenticationService(tickets,accounts,auth,kakao);}
}
