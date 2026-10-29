package com.leets_8th_be.kanghyeonwoo.search.domain;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SearchNormalization {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private SearchNormalization() {}

    public static String normalize(String input) {
        if (input == null) return "";
        return Normalizer.normalize(WHITESPACE.matcher(input).replaceAll(" ").strip()
                .toLowerCase(Locale.ROOT), Normalizer.Form.NFC);
    }

    public static String requireKeyword(String input) {
        String keyword = normalize(input);
        int length = keyword.codePointCount(0, keyword.length());
        if (length < 2 || length > 50) throw new InvalidSearchKeyword();
        return keyword;
    }

    public static boolean canSuggest(String keyword) {
        return keyword.codePoints().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HANGUL
                || c >= 'a' && c <= 'z' || c >= '0' && c <= '9');
    }
}
