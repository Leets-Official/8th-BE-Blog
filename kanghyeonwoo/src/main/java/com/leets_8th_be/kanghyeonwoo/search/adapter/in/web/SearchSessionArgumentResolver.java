package com.leets_8th_be.kanghyeonwoo.search.adapter.in.web;

import com.leets_8th_be.kanghyeonwoo.search.application.port.in.SearchSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class SearchSessionArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String SESSION_KEY = "blog.search.session-key";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == SearchSession.class;
    }

    @Override
    public SearchSession resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                         NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        return () -> resolveKey(request);
    }

    private String resolveKey(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            String key = (String) session.getAttribute(SESSION_KEY);
            if (key == null) {
                key = UUID.randomUUID().toString();
                session.setAttribute(SESSION_KEY, key);
            }
            return key;
        }
    }
}
