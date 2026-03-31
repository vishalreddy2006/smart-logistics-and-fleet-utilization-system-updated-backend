package com.smartlogistics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.smartlogistics.entity.User;
import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.exception.VehicleInUseException;
import com.smartlogistics.repository.TripRepository;
import com.smartlogistics.repository.UserRepository;
import com.smartlogistics.repository.VehicleRepository;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final MaintenancePredictionService maintenancePredictionService;
    private final UserRepository userRepository;

    public VehicleService(VehicleRepository vehicleRepository,
            TripRepository tripRepository,
            MaintenancePredictionService maintenancePredictionService,
            UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.maintenancePredictionService = maintenancePredictionService;
        this.userRepository = userRepository;
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        User user = getCurrentUser();

        // Assign the logged-in user as the vehicle owner
        vehicle.setUser(user);

        // Save vehicle
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getVehiclesForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getName())) {
            return vehicleRepository.findAll();
        }
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) return vehicleRepository.findAll();
        return vehicleRepository.findByUser_Id(user.getId()).stream()
                .filter(vehicle -> vehicle.getUser() != null)
                .collect(Collectors.toList());
    }

    public Vehicle updateVehicle(Long vehicleId, Vehicle vehicleDetails) {
        User user = getCurrentUser();
        Vehicle existingVehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));

        validateOwnership(existingVehicle, user);

        existingVehicle.setVehicleType(vehicleDetails.getVehicleType());
        existingVehicle.setCapacity(vehicleDetails.getCapacity());
        existingVehicle.setMileage(vehicleDetails.getMileage());
        existingVehicle.setAge(vehicleDetails.getAge());
        existingVehicle.setStatus(vehicleDetails.getStatus());
        existingVehicle.setUser(user);

        return vehicleRepository.save(existingVehicle);
    }

    public List<Vehicle> scanFleetMaintenance() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            String email = authentication.getName();

            userId = userRepository.findByEmail(email)
                    .map(user -> user.getId())
                    .orElse(null);
        }

        List<Vehicle> vehicles;

        // Apply user-based filtering
        if (userId != null) {
            vehicles = vehicleRepository.findByUser_Id(userId);
        } else {
            vehicles = vehicleRepository.findAll(); // fallback (optional)
        }

        // KEEP EXISTING LOGIC BELOW THIS LINE (DO NOT MODIFY)
        for (Vehicle vehicle : vehicles) {

            int age = vehicle.getAge() != null ? vehicle.getAge() : 0;
            double mileage = vehicle.getMileage() != null ? vehicle.getMileage() : 0;

            double failureScore = (age * 5) + (mileage / 1000);
            vehicle.setFailureScore(failureScore);

            if (failureScore > 80) {
                vehicle.setMaintenanceRisk("HIGH");
                vehicle.setDaysLeftForService(0);
            } else if (failureScore > 50) {
                vehicle.setMaintenanceRisk("MEDIUM");
                vehicle.setDaysLeftForService(30);
            } else {
                vehicle.setMaintenanceRisk("LOW");
                vehicle.setDaysLeftForService(90);
            }
        }

        return vehicleRepository.saveAll(vehicles);
    }

    public void deleteVehicle(Long vehicleId) {
        User user = getCurrentUser();
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));

        validateOwnership(vehicle, user);

        if (tripRepository.existsByVehicleVehicleId(vehicleId)) {
            throw new VehicleInUseException("Vehicle cannot be deleted because it is assigned to existing trips.");
        }

        vehicleRepository.deleteById(vehicleId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("Unauthorized access");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOwnership(Vehicle vehicle, User user) {
        if (vehicle.getUser() == null || !vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }
    }
}
