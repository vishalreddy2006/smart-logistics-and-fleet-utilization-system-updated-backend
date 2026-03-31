package com.smartlogistics.dto;

import com.smartlogistics.entity.Trip;
import com.smartlogistics.entity.Vehicle;

public class TripRecommendationResponse {

    private Long tripId;
    private String source;
    private String destination;
    private Double distance;
    private Double loadWeight;
    private double predictedFuel;
    private double carbonEmission;
    private Long vehicleId;
    private String vehicleType;
    private double efficiencyScore;
    private String recommendedVehicle;

    public TripRecommendationResponse() {
    }

    public static TripRecommendationResponse fromTrip(Trip trip) {
        TripRecommendationResponse response = new TripRecommendationResponse();
        response.setTripId(trip.getTripId());
        response.setSource(trip.getSource());
        response.setDestination(trip.getDestination());
        response.setDistance(trip.getDistance());
        response.setLoadWeight(trip.getLoadWeight());
        response.setPredictedFuel(trip.getPredictedFuel());
        response.setCarbonEmission(trip.getCarbonEmission());
        response.setEfficiencyScore(trip.getEfficiencyScore());
        response.setRecommendedVehicle(trip.getRecommendedVehicle());

        Vehicle vehicle = trip.getVehicle();
        if (vehicle != null) {
            response.setVehicleId(vehicle.getVehicleId());
            response.setVehicleType(vehicle.getVehicleType());
        }

        return response;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getLoadWeight() {
        return loadWeight;
    }

    public void setLoadWeight(Double loadWeight) {
        this.loadWeight = loadWeight;
    }

    public double getPredictedFuel() {
        return predictedFuel;
    }

    public void setPredictedFuel(double predictedFuel) {
        this.predictedFuel = predictedFuel;
    }

    public double getCarbonEmission() {
        return carbonEmission;
    }

    public void setCarbonEmission(double carbonEmission) {
        this.carbonEmission = carbonEmission;
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

    public double getEfficiencyScore() {
        return efficiencyScore;
    }

    public void setEfficiencyScore(double efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }

    public String getRecommendedVehicle() {
        return recommendedVehicle;
    }

    public void setRecommendedVehicle(String recommendedVehicle) {
        this.recommendedVehicle = recommendedVehicle;
    }
}
