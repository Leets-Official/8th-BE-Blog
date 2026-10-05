package com.example.demo.global.healthcheck.service;

import org.springframework.stereotype.Service;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {

    private static final String HEALTHY = "ok";

    @Override
    public String check() {
        return HEALTHY;
    }
}
