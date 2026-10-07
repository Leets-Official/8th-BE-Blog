package com.leets.mission.service;

import com.leets.mission.dto.StringRepeatRequest;
import com.leets.mission.dto.StringRepeatResponse;
import org.springframework.stereotype.Service;

@Service
public class StringRepeatService {
    public StringRepeatResponse repeatString(StringRepeatRequest request) {
        String inputText = request.getValue();
        return new StringRepeatResponse(inputText, inputText);
    }
}
