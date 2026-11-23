package com.leets_8th_be.kanghyeonwoo.auth.application.port.out;

public interface PasswordHasher {
    String encode(String password);

    boolean matches(String password, String hash);
}
