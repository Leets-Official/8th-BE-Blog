package com.leets_8th_be.kanghyeonwoo.auth.adapter.out.crypto;

import com.leets_8th_be.kanghyeonwoo.auth.application.port.out.OpaqueTokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class SecureOpaqueTokenGenerator implements OpaqueTokenGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
