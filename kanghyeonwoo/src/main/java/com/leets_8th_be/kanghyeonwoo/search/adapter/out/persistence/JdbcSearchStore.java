package com.leets_8th_be.kanghyeonwoo.search.adapter.out.persistence;

import com.leets_8th_be.kanghyeonwoo.search.domain.SearchNormalization;
import com.leets_8th_be.kanghyeonwoo.search.application.port.out.SearchStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcSearchStore implements SearchStore {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final AtomicLong dictionaryVersion = new AtomicLong();
    private final ReentrantLock writes = new ReentrantLock();

    public JdbcSearchStore(JdbcTemplate jdbc, PlatformTransactionManager manager) {
        this.jdbc = jdbc;
        this.transactions = new TransactionTemplate(manager);
        this.transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public long dictionaryVersion() {
        return dictionaryVersion.get();
    }

    public List<Keyword> dictionary() {
        return jdbc.query("SELECT * FROM search_keywords WHERE suggestible = TRUE ORDER BY id", this::keyword);
    }

    public List<Keyword> keywords() {
        return jdbc.query("SELECT * FROM search_keywords ORDER BY id", this::keyword);
    }

    public Keyword find(long id) {
        return jdbc.query("SELECT * FROM search_keywords WHERE id = ?", this::keyword, id)
                .stream().findFirst().orElse(null);
    }

    // This application runs one server. All search writers share this lock through commit.
    public void seedDictionary(List<String> words, Instant at) {
        write(() -> words.forEach(word -> ensureKeyword(word, true, at)));
    }

    public void backfillTitles() {
        write(() -> jdbc.query("SELECT id, title FROM posts WHERE normalized_title IS NULL",
                (rs, row) -> Map.entry(rs.getLong("id"), rs.getString("title")))
                .forEach(post -> jdbc.update("UPDATE posts SET normalized_title = ? WHERE id = ?",
                        SearchNormalization.normalize(post.getValue()), post.getKey())));
    }

    public void record(String word, String sessionKey, boolean hasResults, Instant at) {
        write(() -> {
            Keyword keyword = ensureKeyword(word, hasResults && SearchNormalization.canSuggest(word), at);
            jdbc.queryForObject("SELECT id FROM search_keywords WHERE id = ? FOR UPDATE", Long.class, keyword.id());
            List<Instant> previous = jdbc.query("""
                    SELECT last_counted_at FROM search_dedup_states WHERE keyword_id = ? AND session_key = ?
                    """, (rs, row) -> rs.getObject(1, OffsetDateTime.class).toInstant(), keyword.id(), sessionKey);
            if (!previous.isEmpty() && at.isBefore(previous.getFirst().plusSeconds(300))) return;
            jdbc.update("INSERT INTO search_events(keyword_id, searched_at) VALUES (?, ?)", keyword.id(), time(at));
            incrementBucket(keyword.id(), at);
            jdbc.update("""
                    MERGE INTO search_dedup_states(keyword_id, session_key, last_counted_at)
                    KEY(keyword_id, session_key) VALUES (?, ?, ?)
                    """, keyword.id(), sessionKey, time(at));
        });
    }

    private void incrementBucket(long keywordId, Instant at) {
        OffsetDateTime minute = time(at.truncatedTo(ChronoUnit.MINUTES));
        int changed = jdbc.update("""
                UPDATE search_keyword_buckets SET search_count = search_count + 1
                WHERE keyword_id = ? AND bucket_start = ?
                """, keywordId, minute);
        if (changed == 0) jdbc.update("""
                INSERT INTO search_keyword_buckets(keyword_id, bucket_start, search_count) VALUES (?, ?, 1)
                """, keywordId, minute);
    }

    public void rebuildBuckets() {
        write(() -> {
            jdbc.update("DELETE FROM search_keyword_buckets");
            jdbc.update("""
                    INSERT INTO search_keyword_buckets(keyword_id, bucket_start, search_count)
                    SELECT keyword_id, DATE_TRUNC('MINUTE', searched_at), COUNT(*) FROM search_events
                    GROUP BY keyword_id, DATE_TRUNC('MINUTE', searched_at)
                    """);
        });
    }

    public void cleanup(Instant now) {
        write(() -> {
            Instant cutoff = now.minus(Duration.ofDays(30));
            jdbc.update("DELETE FROM search_events WHERE searched_at < ?", time(cutoff));
            jdbc.update("DELETE FROM search_dedup_states WHERE last_counted_at < ?", time(cutoff));
            jdbc.update("DELETE FROM search_keyword_buckets WHERE bucket_start < ?",
                    time(cutoff.truncatedTo(ChronoUnit.MINUTES)));
            Instant minute = cutoff.truncatedTo(ChronoUnit.MINUTES);
            jdbc.update("DELETE FROM search_keyword_buckets WHERE bucket_start = ?", time(minute));
            jdbc.update("""
                    INSERT INTO search_keyword_buckets(keyword_id, bucket_start, search_count)
                    SELECT keyword_id, ?, COUNT(*) FROM search_events
                    WHERE searched_at >= ? AND searched_at < ? GROUP BY keyword_id
                    """, time(minute), time(minute), time(minute.plus(1, ChronoUnit.MINUTES)));
        });
    }

    private Keyword ensureKeyword(String word, boolean suggestible, Instant at) {
        List<Keyword> matches = jdbc.query("SELECT * FROM search_keywords WHERE normalized_keyword = ?",
                this::keyword, word);
        if (matches.isEmpty()) {
            jdbc.update("INSERT INTO search_keywords(normalized_keyword, suggestible, created_at) VALUES (?, ?, ?)",
                    word, suggestible, time(at));
            if (suggestible) dictionaryChanged();
        } else if (suggestible && !matches.getFirst().suggestible()) {
            jdbc.update("UPDATE search_keywords SET suggestible = TRUE WHERE id = ?", matches.getFirst().id());
            dictionaryChanged();
        }
        return jdbc.queryForObject("SELECT * FROM search_keywords WHERE normalized_keyword = ?", this::keyword, word);
    }

    private void dictionaryChanged() {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { dictionaryVersion.incrementAndGet(); }
        });
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Map<Long, Long> counts(Instant start, Instant end) {
        return counts(start, end, null);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public long count(long keywordId, Instant start, Instant end) {
        return counts(start, end, keywordId).getOrDefault(keywordId, 0L);
    }

    private Map<Long, Long> counts(Instant start, Instant end, Long keywordId) {
        Instant fullStart = start.truncatedTo(ChronoUnit.MINUTES);
        if (fullStart.isBefore(start)) fullStart = fullStart.plus(1, ChronoUnit.MINUTES);
        Instant fullEnd = end.truncatedTo(ChronoUnit.MINUTES);
        if (!fullStart.isBefore(fullEnd)) return rawCounts(start, end, keywordId);
        Map<Long, Long> counts = new HashMap<>(queryCounts("""
                SELECT keyword_id, SUM(search_count) AS total FROM search_keyword_buckets
                WHERE bucket_start >= ? AND bucket_start < ? %s GROUP BY keyword_id
                """, fullStart, fullEnd, keywordId));
        rawCounts(start, fullStart, keywordId).forEach((id, n) -> counts.merge(id, n, Long::sum));
        rawCounts(fullEnd, end, keywordId).forEach((id, n) -> counts.merge(id, n, Long::sum));
        return counts;
    }

    private Map<Long, Long> rawCounts(Instant start, Instant end, Long keywordId) {
        return queryCounts("""
                SELECT keyword_id, COUNT(*) AS total FROM search_events
                WHERE searched_at >= ? AND searched_at < ? %s GROUP BY keyword_id
                """, start, end, keywordId);
    }

    private Map<Long, Long> queryCounts(String sql, Instant start, Instant end, Long keywordId) {
        String filter = keywordId == null ? "" : "AND keyword_id = ?";
        Object[] parameters = keywordId == null ? new Object[]{time(start), time(end)}
                : new Object[]{time(start), time(end), keywordId};
        return jdbc.query(sql.formatted(filter),
                        (rs, row) -> Map.entry(rs.getLong("keyword_id"), rs.getLong("total")), parameters)
                .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Keyword keyword(ResultSet rs, int row) throws SQLException {
        return new Keyword(rs.getLong("id"), rs.getString("normalized_keyword"), rs.getBoolean("suggestible"));
    }

    public void importDemo(List<DemoKeyword> keywords, List<DemoEvent> events,
                           List<DemoDedupState> states, Supplier<Boolean> createPosts) {
        write(() -> {
            if (!createPosts.get()) return;
            Map<Long, Long> ids = new HashMap<>();
            for (DemoKeyword keyword : keywords) {
                Keyword saved = ensureKeyword(keyword.keyword(), keyword.suggestible(), keyword.createdAt());
                ids.put(keyword.sourceId(), saved.id());
            }
            for (DemoEvent event : events) {
                long id = ids.get(event.sourceKeywordId());
                jdbc.update("INSERT INTO search_events(keyword_id, searched_at) VALUES (?, ?)", id, time(event.searchedAt()));
                incrementBucket(id, event.searchedAt());
            }
            for (DemoDedupState state : states) {
                jdbc.update("""
                        INSERT INTO search_dedup_states(keyword_id, session_key, last_counted_at) VALUES (?, ?, ?)
                        """, ids.get(state.sourceKeywordId()), state.sessionKey(), time(state.lastCountedAt()));
            }
        });
    }

    private void write(Runnable operation) {
        writes.lock();
        try {
            transactions.executeWithoutResult(status -> operation.run());
        } finally {
            writes.unlock();
        }
    }

    private static OffsetDateTime time(Instant at) {
        return at.atOffset(ZoneOffset.UTC);
    }
}
