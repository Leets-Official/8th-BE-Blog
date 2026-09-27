package org.example._thbe.domain.StringRepeat.service;

import org.example._thbe.domain.StringRepeat.dto.StringRepeatRequest;
import org.example._thbe.domain.StringRepeat.dto.StringRepeatResponse;
import org.springframework.stereotype.Service;

@Service
public class StringRepeatService {

    public StringRepeatResponse repeatString(StringRepeatRequest request) {
        return StringRepeatResponse.of(request.getValue(), request.getValue());
    }
}
