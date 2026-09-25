package com.example.demo.service;

import com.example.demo.dto.RepeatStringResponse;
import org.springframework.stereotype.Service;

@Service
public class AssignmentService {
    public RepeatStringResponse repeat(String string){
        RepeatStringResponse response = new RepeatStringResponse(string, string);
        return response;
    }
}
