package com.leets_8th_be.kanghyeonwoo.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/** Upgrades the existing H2 schema before Hibernate adds non-null fields and constraints. */
public class ModerationSchemaMigration {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    public ModerationSchemaMigration(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public void migrate() throws SQLException {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             var rows = connection.getMetaData().getTables(null, connection.getSchema(), null, new String[]{"TABLE"})) {
            while (rows.next()) tables.add(rows.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
        }
        if (tables.contains("reports")) {
            var duplicates = jdbc.queryForList("""
                    SELECT reporter_id, target_type, target_id, COUNT(*) AS duplicate_count
                    FROM reports GROUP BY reporter_id, target_type, target_id HAVING COUNT(*) > 1
                    """);
            if (!duplicates.isEmpty()) {
                throw new IllegalStateException("중복 신고가 있습니다. 원본을 보존하고 중복을 확인한 뒤 재시작하세요: " + duplicates);
            }
        }
        for (String table : new String[]{"posts", "comments", "reports"}) {
            if (!tables.contains(table)) continue;
            String initial = table.equals("reports") ? "PENDING" : "ACTIVE";
            jdbc.execute("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT '" + initial + "'");
            jdbc.update("UPDATE " + table + " SET status = ? WHERE status IS NULL", initial);
            jdbc.execute("ALTER TABLE " + table + " ALTER COLUMN status SET NOT NULL");
        }
        if (tables.contains("reports")) {
            jdbc.execute("ALTER TABLE reports ADD CONSTRAINT IF NOT EXISTS uk_report_reporter_target UNIQUE (reporter_id, target_type, target_id)");
        }
    }
}
