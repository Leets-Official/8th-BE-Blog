package com.leets_8th_be.kanghyeonwoo.config;

import com.leets_8th_be.kanghyeonwoo.KanghyeonwooApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Seeds the configured local database and closes it without starting an HTTP server. */
@Slf4j
public class MockDataLoader {
    public static void main(String[] args) {
        try (var context = new SpringApplicationBuilder(KanghyeonwooApplication.class)
                .web(WebApplicationType.NONE).profiles("mock-data").run(args)) {
            log.info("목 데이터 저장 완료: {}", context.getEnvironment().getProperty("spring.datasource.url"));
        }
    }
}
