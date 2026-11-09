package com.leets_8th_be.kanghyeonwoo.auth;
import com.leets_8th_be.kanghyeonwoo.auth.application.*;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationPorts.*;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration
public class AuthenticationAdapters {
 @Bean Accounts authenticationAccounts(AccountDirectory accounts) {
  return new Accounts() {
   private Account view(com.leets_8th_be.kanghyeonwoo.user.application.contract.Credentials c) {return new Account(c.userId(),c.passwordHash(),c.kakaoId(),c.role());}
   public java.util.Optional<Account> byEmail(String email) {return accounts.byEmail(email).map(this::view);}
   public java.util.Optional<Account> byKakaoId(String id) {return accounts.byKakaoId(id).map(this::view);}
   public boolean emailExists(String email) {return accounts.emailExists(email);}
   public Account register(String email,String nickname,String hash,String kakao) {return view(accounts.register(email,nickname,hash,kakao));}
  };
 }
 @Bean Passwords authenticationPasswords(PasswordEncoder encoder) {return new Passwords() {
  public String encode(String raw) {return encoder.encode(raw);}public boolean matches(String raw,String hash) {return encoder.matches(raw,hash);}
 };}
 @Bean Sessions authenticationSessions(LoginSessions repository) {return new Sessions() {
  public java.util.Optional<Session> lock(String id) {return repository.lock(id).map(row->{var s=new Session(row.id,row.userId);s.refreshHash=row.refreshHash;s.expiresAt=row.expiresAt;s.revoked=row.revoked;return s;});}
  public void save(Session s) {var row=repository.findById(s.id).orElseGet(()->new LoginSession(s.id,s.userId));row.refreshHash=s.refreshHash;row.expiresAt=s.expiresAt;row.revoked=s.revoked;repository.save(row);}
 };}
 @Bean AuthenticationService authenticationService(Accounts accounts,Passwords passwords,JwtTokens tokens,Sessions sessions) {return new AuthenticationService(accounts,passwords,tokens,sessions);}
}
