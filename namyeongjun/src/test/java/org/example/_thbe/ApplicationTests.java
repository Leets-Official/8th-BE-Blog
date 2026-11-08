package org.example._thbe;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-test-secret-key-test-secret-key",   // 32바이트 이상
        "jwt.access-expiration=1800000",
        "jwt.refresh-expiration=1209600000"
})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
