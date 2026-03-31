package com.smartlogistics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartlogistics.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

	boolean existsByVehicleVehicleId(Long vehicleId);

	List<Trip> findByUserId(Long userId);
}
