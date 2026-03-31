package com.smartlogistics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartlogistics.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatus(String status);

    List<Vehicle> findByMaintenanceRisk(String maintenanceRisk);

    List<Vehicle> findByUser_IdAndMaintenanceRisk(Long userId, String risk);

    List<Vehicle> findByUser_Id(Long userId);

    List<Vehicle> findByUserIsNull();
}
