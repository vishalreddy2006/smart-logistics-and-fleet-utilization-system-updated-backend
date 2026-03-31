package com.smartlogistics.dto;

import com.smartlogistics.entity.Vehicle;

public class MaintenanceAlertResponse {

    private Long vehicleId;
    private String vehicleType;
    private Integer age;
    private Double mileage;
    private String maintenanceRisk;

    public MaintenanceAlertResponse() {
    }

    public static MaintenanceAlertResponse fromVehicle(Vehicle vehicle) {
        MaintenanceAlertResponse response = new MaintenanceAlertResponse();
        response.setVehicleId(vehicle.getVehicleId());
        response.setVehicleType(vehicle.getVehicleType());
        response.setAge(vehicle.getAge());
        response.setMileage(vehicle.getMileage());
        response.setMaintenanceRisk(vehicle.getMaintenanceRisk());
        return response;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getMileage() {
        return mileage;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public String getMaintenanceRisk() {
        return maintenanceRisk;
    }

    public void setMaintenanceRisk(String maintenanceRisk) {
        this.maintenanceRisk = maintenanceRisk;
    }
}
