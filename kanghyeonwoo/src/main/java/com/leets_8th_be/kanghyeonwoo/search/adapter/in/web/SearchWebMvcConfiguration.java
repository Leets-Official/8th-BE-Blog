package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SearchWebMvcConfiguration implements WebMvcConfigurer {
    private final SearchSessionArgumentResolver searchSessions;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(searchSessions);
    }
}
