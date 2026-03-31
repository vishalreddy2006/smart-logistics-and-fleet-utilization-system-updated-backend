package com.smartlogistics.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tripId;

    private String source;
    private String destination;
    private Double distance;
    private Double loadWeight;

    @Column(name = "predicted_fuel")
    private double predictedFuel;

    @Column(name = "carbon_emission")
    private double carbonEmission;

    @Column(name = "efficiency_score")
    private Double efficiencyScore;

    @Column(name = "recommended_vehicle")
    private String recommendedVehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
    private Vehicle vehicle;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Trip() {
    }

    public Trip(Long tripId, String source, String destination, Double distance, Double loadWeight, double predictedFuel, double carbonEmission, Vehicle vehicle) {
        this.tripId = tripId;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.loadWeight = loadWeight;
        this.predictedFuel = predictedFuel;
        this.carbonEmission = carbonEmission;
        this.vehicle = vehicle;
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

    public Double getEfficiencyScore() {
        return efficiencyScore != null ? efficiencyScore : 0.0;
    }

    public void setEfficiencyScore(Double efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }

    public String getRecommendedVehicle() {
        return recommendedVehicle;
    }

    public void setRecommendedVehicle(String recommendedVehicle) {
        this.recommendedVehicle = recommendedVehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
