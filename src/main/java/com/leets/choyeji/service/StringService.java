package com.leets.choyeji.service;

import com.leets.choyeji.dto.StringRepeatResponse;
import org.springframework.stereotype.Service;

@Service
public class StringService {

    public StringRepeatResponse repeat(String value) {
        return new StringRepeatResponse(value, value);
    }
}
