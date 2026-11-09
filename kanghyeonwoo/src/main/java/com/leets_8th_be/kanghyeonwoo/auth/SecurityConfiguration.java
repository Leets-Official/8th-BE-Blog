package com.leets_8th_be.kanghyeonwoo.auth;
import com.leets_8th_be.kanghyeonwoo.user.application.port.in.AccountDirectory;
import com.leets_8th_be.kanghyeonwoo.shared.web.ApiResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
@Configuration
@SecurityScheme(name="BearerAuth",type=SecuritySchemeType.HTTP,scheme="bearer",bearerFormat="JWT")
public class SecurityConfiguration {
 @Bean PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
 @org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication(type=org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET)
 @Bean SecurityFilterChain security(HttpSecurity http,JwtTokens jwt,AccountDirectory accounts,ObjectMapper json) throws Exception {
  return http.csrf(c->c.disable()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .requestCache(c->c.disable()).formLogin(c->c.disable()).httpBasic(c->c.disable())
   .authorizeHttpRequests(a->a.requestMatchers("/api/v2/auth/**","/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**").permitAll()
    .requestMatchers(HttpMethod.GET,"/api/v2/reports").hasRole("ADMIN")
    .requestMatchers("/api/v2/reports/*/resolve").hasRole("ADMIN").anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((r,s,x)->JwtAuthenticationFilter.write(s,401,ApiResponseCode.UNAUTHORIZED,json)).accessDeniedHandler((r,s,x)->JwtAuthenticationFilter.write(s,403,ApiResponseCode.ACCESS_DENIED,json)))
   .addFilterBefore(new JwtAuthenticationFilter(jwt,accounts,json),UsernamePasswordAuthenticationFilter.class).build();
 }
}
