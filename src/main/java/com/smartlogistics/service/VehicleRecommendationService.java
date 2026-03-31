package com.smartlogistics.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.exception.ResourceNotFoundException;
import com.smartlogistics.repository.VehicleRepository;
import com.smartlogistics.util.FuelPredictionService;

@Service
public class VehicleRecommendationService {

    private final VehicleRepository vehicleRepository;
    private final FuelPredictionService fuelPredictionService;

    public VehicleRecommendationService(VehicleRepository vehicleRepository, FuelPredictionService fuelPredictionService) {
        this.vehicleRepository = vehicleRepository;
        this.fuelPredictionService = fuelPredictionService;
    }

    public Vehicle recommendVehicle(double distance, double loadWeight) {
        return recommendVehicle(distance, loadWeight, vehicleRepository.findAll());
    }

    public Vehicle recommendVehicle(double distance, double loadWeight, List<Vehicle> vehicles) {
        List<Vehicle> candidates = vehicles.stream()
                .filter(v -> v.getCapacity() != null && v.getCapacity() >= loadWeight)
                .toList();

        return candidates.stream()
                .map(v -> new ScoredVehicle(v, calculateScore(v, distance, loadWeight)))
                .max(Comparator.comparingDouble(ScoredVehicle::score))
                .map(ScoredVehicle::vehicle)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No vehicle found with sufficient capacity for load weight: " + loadWeight));
    }

    private double calculateScore(Vehicle vehicle, double distance, double loadWeight) {
        double capacity = vehicle.getCapacity() != null ? vehicle.getCapacity() : 0.0;
        double mileage  = vehicle.getMileage()  != null ? vehicle.getMileage()  : 0.0;
        int    age      = vehicle.getAge()       != null ? vehicle.getAge()      : 0;

        double predictedFuel = fuelPredictionService.predictFuel(distance, loadWeight, age);
        return (mileage * 0.5) + (capacity * 0.3) - (age * 0.2) - predictedFuel;
    }

    private record ScoredVehicle(Vehicle vehicle, double score) {}
}
