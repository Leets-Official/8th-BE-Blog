package com.leets.blog.service;

import com.leets.blog.dto.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {

    public String health() {
        return "ok";
    }

    public RepeatStringResponse repeat(String value) {
        return new RepeatStringResponse(value, value);
    }
}
