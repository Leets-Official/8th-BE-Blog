package com.leets.mission.service;

import com.leets.mission.dto.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {
    public RepeatStringResponse repeat(String value){
        return new RepeatStringResponse(value,value);
    }
}
