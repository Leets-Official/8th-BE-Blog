package com.leets_8th_be.kanghyeonwoo.shared.adapter.out.persistence;

import java.util.Locale;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.query.criteria.JpaExpression;

public final class TextSearch {
    private TextSearch() {}

    public static Predicate contains(CriteriaBuilder cb, Expression<String> field, String keyword) {
        // Hibernate requires a SQL string cast before applying lower() to a CLOB.
        return cb.like(cb.lower(((JpaExpression<String>) field).cast(String.class)), containsPattern(keyword), '!');
    }

    private static String containsPattern(String keyword) {
        return "%" + keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_") + "%";
    }
}
