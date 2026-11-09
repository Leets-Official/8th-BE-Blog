package com.leets_8th_be.kanghyeonwoo.controller;

import com.leets_8th_be.kanghyeonwoo.KanghyeonwooApplication;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchSuggestionService;
import com.leets_8th_be.kanghyeonwoo.search.application.usecase.SearchTrendService;
import com.leets_8th_be.kanghyeonwoo.search.application.model.SearchTrend;
import com.leets_8th_be.kanghyeonwoo.search.application.port.in.SearchPostsUseCase;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class SearchPersistenceTest {
    @TempDir Path directory;

    @Test
    void restartingKeepsTheHistoryAndDoesNotInsertDemoDataTwice() {
        for (int i = 0; i < 2; i++) {
            try (var context = new SpringApplicationBuilder(KanghyeonwooApplication.class,
                    SearchAnalyticsTest.TimeConfiguration.class).run(
                    "--spring.main.web-application-type=none", "--spring.profiles.active=demo",
                    "--spring.datasource.url=jdbc:h2:file:" + directory.resolve("blog"),
                    "--spring.jpa.hibernate.ddl-auto=update", "--spring.jpa.show-sql=false")) {
                assertThat(context.getBean(SearchTrendService.class).trends()).extracting(SearchTrend::keyword)
                        .containsExactly("spring", "jpa", "java");
                assertThat(context.getBean(SearchTrendService.class).trends().getFirst().currentCount()).isEqualTo(10);
                assertThat(context.getBean(SearchSuggestionService.class).suggest("sprng").getFirst().keyword()).isEqualTo("spring");
                assertThat(context.getBean(SearchPostsUseCase.class).search(1, 10, null).items()).hasSize(2);
            }
        }
    }
}
