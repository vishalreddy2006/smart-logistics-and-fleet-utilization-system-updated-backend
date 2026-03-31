package com.smartlogistics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartlogistics.entity.Trip;
import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.repository.TripRepository;
import com.smartlogistics.repository.VehicleRepository;
import com.smartlogistics.util.FuelPredictionService;
import com.smartlogistics.util.UserContext;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleRecommendationService vehicleRecommendationService;
    private final FuelPredictionService fuelPredictionService;
    private final CarbonEmissionService carbonEmissionService;
    private final UserContext userContext;

    public TripService(
            TripRepository tripRepository,
            VehicleRepository vehicleRepository,
            VehicleRecommendationService vehicleRecommendationService,
            FuelPredictionService fuelPredictionService,
            CarbonEmissionService carbonEmissionService,
            UserContext userContext
    ) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleRecommendationService = vehicleRecommendationService;
        this.fuelPredictionService = fuelPredictionService;
        this.carbonEmissionService = carbonEmissionService;
        this.userContext = userContext;
    }

    public Trip createTrip(Trip trip) {
        Long currentUserId = userContext.getCurrentUserIdOrNull();

        double distance = trip.getDistance() != null ? trip.getDistance() : 0.0;
        double loadWeight = trip.getLoadWeight() != null ? trip.getLoadWeight() : 0.0;

        // Resolve the vehicle: prefer user-selected vehicle; fall back to AI recommendation
        Vehicle assignedVehicle = null;

        if (trip.getVehicle() != null) {
            Long vehicleId = trip.getVehicle().getVehicleId();
            if (vehicleId != null) {
                assignedVehicle = vehicleRepository.findById(vehicleId).orElse(null);

                if (assignedVehicle != null && currentUserId != null
                        && (assignedVehicle.getUser() == null
                        || !assignedVehicle.getUser().getId().equals(currentUserId))) {
                    throw new RuntimeException("Unauthorized access: You can only create trips with your own vehicles");
                }
            }
        }

        if (assignedVehicle == null) {
            List<Vehicle> availableVehicles;
            if (currentUserId != null) {
                availableVehicles = vehicleRepository.findByUser_Id(currentUserId);
                availableVehicles = availableVehicles.stream()
                        .filter(v -> "AVAILABLE".equals(v.getStatus()))
                        .toList();
            } else {
                availableVehicles = vehicleRepository.findByStatus("AVAILABLE");
            }
            assignedVehicle = vehicleRecommendationService.recommendVehicle(distance, loadWeight, availableVehicles);
        }

        int vehicleAge = assignedVehicle.getAge() != null ? assignedVehicle.getAge() : 0;
        double mileage = assignedVehicle.getMileage() != null ? assignedVehicle.getMileage() : 5.0;

        double predictedFuel = fuelPredictionService.predictFuel(distance, loadWeight, vehicleAge, mileage);
        double carbonEmission = carbonEmissionService.calculateEmission(predictedFuel);

        // Efficiency score: compare expected (base) vs actual (LR) fuel
        double safeMileage = mileage > 0 ? mileage : 1.0;
        double baseFuel = distance / safeMileage;
        double expectedFuel = baseFuel * (1 + loadWeight * 0.05 + vehicleAge * 0.03);
        double efficiencyScore = (expectedFuel / predictedFuel) * 100;
        efficiencyScore = Math.min(100, Math.max(0, efficiencyScore));

        // Load-based vehicle recommendation
        String recommendedVehicle;
        if (loadWeight > 8) {
            recommendedVehicle = "Truck";
        } else if (loadWeight > 3) {
            recommendedVehicle = "Mini Truck";
        } else {
            recommendedVehicle = "Van";
        }

        trip.setPredictedFuel(predictedFuel);
        trip.setCarbonEmission(carbonEmission);
        trip.setEfficiencyScore((Double) efficiencyScore);
        trip.setRecommendedVehicle(recommendedVehicle);
        trip.setVehicle(assignedVehicle);

        if (currentUserId != null) {
            trip.setUserId(currentUserId);
        }

        return tripRepository.save(trip);
    }

    public List<Trip> getAllTrips() {
        Long currentUserId = userContext.getCurrentUserIdOrNull();
        if (currentUserId != null) {
            return tripRepository.findByUserId(currentUserId);
        } else {
            return tripRepository.findAll();
        }
    }
}
