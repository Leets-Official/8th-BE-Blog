package com.leets.mission.controller;

import com.leets.mission.dto.StringRepeatRequest;
import com.leets.mission.dto.StringRepeatResponse;
import com.leets.mission.service.StringRepeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StringRepeatController {

    private final StringRepeatService stringRepeatService;

    @PostMapping("/string/repeat")
    public StringRepeatResponse repeatString(@RequestBody StringRepeatRequest request) {
        return stringRepeatService.repeatString(request);
    }
}
