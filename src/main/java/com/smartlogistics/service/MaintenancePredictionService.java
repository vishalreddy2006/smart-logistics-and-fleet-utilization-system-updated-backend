package com.smartlogistics.service;

import org.springframework.stereotype.Service;

@Service
public class MaintenancePredictionService {

    public String predictRisk(int age, double mileage) {
        if (age > 5 || mileage < 10) {
            return "HIGH";
        }

        if (age >= 3 && age <= 5) {
            return "MEDIUM";
        }

        return "LOW";
    }
}
