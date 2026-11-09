package com.leets_8th_be.kanghyeonwoo.auth;
import com.leets_8th_be.kanghyeonwoo.auth.application.KakaoAuthenticationService;
import com.leets_8th_be.kanghyeonwoo.auth.application.KakaoAuthenticationService.*;
import com.leets_8th_be.kanghyeonwoo.auth.application.AuthenticationService.Tokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class KakaoAuthService {
 private final KakaoAuthenticationService service;
 public KakaoAuthService(KakaoAuthenticationService service) {this.service=service;}
 @Transactional public Start start() {return service.start();}
 @Transactional(noRollbackFor=AuthFailure.class) public String callback(String state,String browser,String code) {return service.callback(state,browser,code);}
 @Transactional public Exchange exchange(String code,String browser) {return service.exchange(code,browser);}
 @Transactional public Tokens signup(String token,String browser,String nickname) {return service.signup(token,browser,nickname);}
}
