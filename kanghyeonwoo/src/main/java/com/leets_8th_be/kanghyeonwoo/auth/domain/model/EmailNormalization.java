package com.leets_8th_be.kanghyeonwoo.auth.domain.model;

import java.util.Locale;

public final class EmailNormalization {
    private EmailNormalization() {}

    public static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
