package com.smartlogistics.service;

import org.springframework.stereotype.Service;

@Service
public class CarbonEmissionService {

    public double calculateEmission(double fuel) {
        return fuel * 2.68;
    }
}
