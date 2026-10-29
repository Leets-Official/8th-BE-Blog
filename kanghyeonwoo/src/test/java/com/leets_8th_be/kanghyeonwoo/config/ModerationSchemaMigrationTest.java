package com.leets_8th_be.kanghyeonwoo.config;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.assertj.core.api.Assertions.*;

class ModerationSchemaMigrationTest {
    @Test
    void backfillsLegacyRowsAndEnforcesReportUniquenessWithoutDeletingData() throws Exception {
        var source = legacyDatabase();
        var jdbc = new JdbcTemplate(source);
        jdbc.update("insert into reports values (1, 2, 'POST', 1)");
        var migration = new ModerationSchemaMigration(source);
        migration.migrate();
        migration.migrate();
        assertThat(jdbc.queryForObject("select status from posts where id=1", String.class)).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("select status from comments where id=1", String.class)).isEqualTo("ACTIVE");
        assertThat(jdbc.queryForObject("select status from reports where id=1", String.class)).isEqualTo("PENDING");
        assertThatThrownBy(() -> jdbc.update("insert into reports(id, reporter_id, target_type, target_id) values (2, 2, 'POST', 1)"))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThat(jdbc.queryForObject("select count(*) from reports", Long.class)).isEqualTo(1L);
    }

    @Test
    void reportsLegacyDuplicatesAndKeepsEveryReport() {
        var source = legacyDatabase();
        var jdbc = new JdbcTemplate(source);
        jdbc.update("insert into reports values (1, 2, 'POST', 1), (2, 2, 'POST', 1)");
        assertThatThrownBy(() -> new ModerationSchemaMigration(source).migrate())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("중복 신고");
        assertThat(jdbc.queryForObject("select count(*) from reports", Long.class)).isEqualTo(2L);
    }

    private DriverManagerDataSource legacyDatabase() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = new JdbcTemplate(source);
        jdbc.execute("create table posts(id bigint primary key)");
        jdbc.execute("create table comments(id bigint primary key)");
        jdbc.execute("create table reports(id bigint primary key, reporter_id bigint, target_type varchar(20), target_id bigint)");
        jdbc.update("insert into posts values (1)");
        jdbc.update("insert into comments values (1)");
        return source;
    }
}
