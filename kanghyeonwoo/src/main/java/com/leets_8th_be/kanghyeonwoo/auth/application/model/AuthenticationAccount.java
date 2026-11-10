package com.leets_8th_be.kanghyeonwoo.auth.application.model;

public record AuthenticationAccount(long userId, String passwordHash, String kakaoId, String role) {}
