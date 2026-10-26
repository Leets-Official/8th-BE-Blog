package com.leets_8th_be.kanghyeonwoo.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leets_8th_be.kanghyeonwoo.entity.Post;
import com.leets_8th_be.kanghyeonwoo.entity.User;
import com.leets_8th_be.kanghyeonwoo.repository.PostRepository;
import com.leets_8th_be.kanghyeonwoo.repository.UserRepository;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
@Order(20)
@RequiredArgsConstructor
public class SearchDemoSeeder implements ApplicationRunner {
    private static final String DEMO_EMAIL = "search-demo@blog.example";
    private final SearchStore store;
    private final SearchSuggestionService suggestions;
    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final PostRepository posts;
    private final ObjectMapper mapper;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JsonNode example;
        try (InputStream input = new ClassPathResource("demo/search-example.json").getInputStream()) {
            example = mapper.readTree(input);
        }
        Instant anchor = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        Duration shift = Duration.between(Instant.parse(example.get("reference_time").asText()), anchor);
        store.importDemo(() -> {
            if (jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Long.class, DEMO_EMAIL) > 0) return;
            User author = users.saveAndFlush(User.create("검색 데모 작성자", DEMO_EMAIL));
            posts.save(Post.create("Spring Boot와 Java 시작하기", "검색 추천과 집계를 확인하는 데모 게시글입니다.", author));
            posts.save(Post.create("JPA와 스프링부트 실습", "스프링과 JPA 검색 결과를 확인합니다.", author));
            Map<Long, Long> ids = new HashMap<>();
            for (JsonNode keyword : example.get("search_keywords")) {
                SearchStore.Keyword saved = store.ensureKeyword(keyword.get("normalized_keyword").asText(),
                        keyword.get("suggestible").asBoolean(), shifted(keyword.get("created_at"), shift));
                ids.put(keyword.get("id").asLong(), saved.id());
            }
            for (JsonNode event : example.get("search_events")) {
                long id = ids.get(event.get("keyword_id").asLong());
                Instant at = shifted(event.get("searched_at"), shift);
                jdbc.update("INSERT INTO search_events(keyword_id, searched_at) VALUES (?, ?)", id, SearchStore.time(at));
                store.incrementBucket(id, at);
            }
            for (JsonNode state : example.get("search_dedup_states")) {
                jdbc.update("""
                        INSERT INTO search_dedup_states(keyword_id, session_key, last_counted_at) VALUES (?, ?, ?)
                        """, ids.get(state.get("keyword_id").asLong()), state.get("session_key").asText(),
                        SearchStore.time(shifted(state.get("last_counted_at"), shift)));
            }
        });
        suggestions.rebuild();
    }

    private Instant shifted(JsonNode value, Duration shift) {
        return Instant.parse(value.asText()).plus(shift);
    }
}
