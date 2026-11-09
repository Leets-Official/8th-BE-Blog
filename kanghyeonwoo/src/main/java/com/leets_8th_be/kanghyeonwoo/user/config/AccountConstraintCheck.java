package com.leets_8th_be.kanghyeonwoo.user.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountConstraintCheck {
    private static final Map<String, String> REQUIRED_INDEXES = Map.of(
            "uk_user_email", "email",
            "uk_user_nickname", "username");

    @Bean
    ApplicationRunner verifyAccountUniqueness(JdbcTemplate jdbc) {
        return args -> {
            verifyNoDuplicateUsers(jdbc);
            jdbc.execute((ConnectionCallback<Void>) this::ensureUniqueIndexes);
        };
    }

    private void verifyNoDuplicateUsers(JdbcTemplate jdbc) {
        for (String expression : List.of("lower(trim(email))", "trim(username)")) {
            String query =
                    "select %s, count(*) from users group by %s having count(*) > 1".formatted(expression, expression);
            if (!jdbc.queryForList(query).isEmpty()) {
                throw new IllegalStateException("기존 사용자 중복 데이터를 먼저 정리해 주세요. docs/auth-security.md의 사전 점검 SQL을 참고하세요.");
            }
        }
    }

    private Void ensureUniqueIndexes(Connection connection) throws SQLException {
        Set<String> existingIndexes = findUniqueIndexes(connection);
        for (var required : REQUIRED_INDEXES.entrySet()) {
            String indexName = required.getKey();
            boolean exists = existingIndexes.stream().anyMatch(name -> matchesIndex(name, indexName));
            if (!exists) {
                String sql = "CREATE UNIQUE INDEX %s ON users(%s)".formatted(indexName, required.getValue());
                try (var statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }
        }
        return null;
    }

    private Set<String> findUniqueIndexes(Connection connection) throws SQLException {
        Set<String> names = new HashSet<>();
        var metadata = connection.getMetaData();
        for (String table : List.of("users", "USERS")) {
            try (var indexes = metadata.getIndexInfo(connection.getCatalog(), null, table, true, false)) {
                while (indexes.next()) {
                    String name = indexes.getString("INDEX_NAME");
                    if (name != null) {
                        names.add(name.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        return names;
    }

    private boolean matchesIndex(String actualName, String requiredName) {
        return actualName.equals(requiredName) || actualName.startsWith(requiredName + "_index_");
    }
}
