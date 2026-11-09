package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-secret-key-for-leets-week6-jwt-0123456789")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
