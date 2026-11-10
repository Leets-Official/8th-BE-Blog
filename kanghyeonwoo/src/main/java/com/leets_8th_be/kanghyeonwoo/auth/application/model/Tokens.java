package com.leets_8th_be.kanghyeonwoo.auth.application.model;

public record Tokens(String accessToken, String refreshToken, long expiresIn) {}
