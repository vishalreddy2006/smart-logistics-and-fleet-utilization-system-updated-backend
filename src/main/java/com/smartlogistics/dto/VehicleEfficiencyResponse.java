package com.smartlogistics.dto;

public class VehicleEfficiencyResponse {

    private final Long vehicleId;
    private final String vehicleType;
    private final double costPerKm;
    private final double fuelEfficiency;
    private final int tripCount;

    public VehicleEfficiencyResponse(Long vehicleId, String vehicleType,
                                     double costPerKm, double fuelEfficiency, int tripCount) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.costPerKm = costPerKm;
        this.fuelEfficiency = fuelEfficiency;
        this.tripCount = tripCount;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public double getCostPerKm() {
        return costPerKm;
    }

    public double getFuelEfficiency() {
        return fuelEfficiency;
    }

    public int getTripCount() {
        return tripCount;
    }
}
