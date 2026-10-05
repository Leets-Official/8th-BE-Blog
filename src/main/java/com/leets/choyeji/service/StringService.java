package com.leets.blog.service;

import com.leets.blog.dto.StringRepeatResponse;
import org.springframework.stereotype.Service;

@Service
public class StringService {

    public StringRepeatResponse repeat(String value) {
        return new StringRepeatResponse(value, value);
    }
}
