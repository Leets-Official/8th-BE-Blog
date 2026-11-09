package com.leets_8th_be.kanghyeonwoo.user.application.contract;

public record Credentials(long userId, String passwordHash, String kakaoId, String role) {}
