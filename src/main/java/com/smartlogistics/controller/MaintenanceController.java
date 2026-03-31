package com.smartlogistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.service.MaintenanceService;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getMaintenanceData() {
        List<Vehicle> vehicles = maintenanceService.getMaintenanceData();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/predict")
    public ResponseEntity<List<Vehicle>> predictMaintenance() {
        List<Vehicle> updatedVehicles = maintenanceService.predictMaintenance();
        return ResponseEntity.ok(updatedVehicles);
    }
}
