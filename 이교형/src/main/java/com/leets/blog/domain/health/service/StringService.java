package com.leets.blog.domain.health.service;

import com.leets.blog.domain.health.dto.StringResponse;
import org.springframework.stereotype.Service;

@Service
public class StringService {

    public StringResponse repeat(String value) {

        return new StringResponse(value, value);
    }
}