package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");
    private final String claim;

    TokenType(String claim) {
        this.claim = claim;
    }

    public String claim() {
        return claim;
    }
}
