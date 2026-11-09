package com.leets_8th_be.kanghyeonwoo.auth;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import com.leets_8th_be.kanghyeonwoo.shared.web.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.io.IOException;
import java.util.List;
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtTokens jwt;private final AccountDirectory accounts;private final ObjectMapper json;
 public JwtAuthenticationFilter(JwtTokens j,AccountDirectory a,ObjectMapper o) {jwt=j;accounts=a;json=o;}
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
  String header=req.getHeader("Authorization");
  if(header!=null) {
   try {
    if(!header.regionMatches(true,0,"Bearer ",0,7)) throw new AuthFailure(ApiResponseCode.UNAUTHORIZED);
    var c=jwt.parse(header.substring(7),"access");var account=accounts.byId(Long.parseLong(c.getSubject()));
    if(account==null) throw new AuthFailure(ApiResponseCode.UNAUTHORIZED);
    var context=SecurityContextHolder.createEmptyContext();context.setAuthentication(new UsernamePasswordAuthenticationToken(Long.toString(account.userId()),null,List.of(new SimpleGrantedAuthority("ROLE_"+account.role()))));SecurityContextHolder.setContext(context);
   } catch(AuthFailure | NumberFormatException e) { SecurityContextHolder.clearContext();write(res,401,ApiResponseCode.UNAUTHORIZED,json);return; }
  }
  chain.doFilter(req,res);
 }
 static void write(HttpServletResponse res,int status,ApiResponseCode code,ObjectMapper json) throws IOException {res.setStatus(status);res.setContentType("application/json;charset=UTF-8");json.writeValue(res.getOutputStream(),ApiResponse.fail(code,code.getDefaultMessage()));}
}
