package com.smartlogistics.repository.custom;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.repository.VehicleRepository;
import com.smartlogistics.util.UserContext;

import jakarta.persistence.EntityManager;

/**
 * Custom repository implementation that adds authorization checks
 * for Vehicle updates and deletes.
 */
@Component
public class VehicleRepositoryCustom {

    private final VehicleRepository vehicleRepository;
    private final UserContext userContext;
    private final EntityManager entityManager;

    public VehicleRepositoryCustom(VehicleRepository vehicleRepository, UserContext userContext,
            EntityManager entityManager) {
        this.vehicleRepository = vehicleRepository;
        this.userContext = userContext;
        this.entityManager = entityManager;
    }

    /**
     * Save a vehicle with ownership verification for updates.
     * For new vehicles, userId is auto-set by EntityListener.
     * For updates, verifies the current user owns the vehicle.
     */
    public Vehicle saveWithOwnershipCheck(Vehicle vehicle) {
        // Get the existing vehicle from DB if it exists
        if (vehicle.getVehicleId() != null) {
            Optional<Vehicle> existing = vehicleRepository.findById(vehicle.getVehicleId());

            if (existing.isPresent()) {
                Long currentUserId = userContext.getCurrentUserIdOrNull();
                Vehicle existingVehicle = existing.get();

                // Check ownership for update
                if (currentUserId != null && (existingVehicle.getUser() == null
                        || !existingVehicle.getUser().getId().equals(currentUserId))) {
                    throw new RuntimeException("Unauthorized access: You can only update your own vehicles");
                }
            }
        }

        return vehicleRepository.save(vehicle);
    }
}
