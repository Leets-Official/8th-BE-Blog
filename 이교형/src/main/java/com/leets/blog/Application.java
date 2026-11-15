package com.leets.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)   // JWT만 쓰므로 기본 로그인 계정 생성 안 함
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
