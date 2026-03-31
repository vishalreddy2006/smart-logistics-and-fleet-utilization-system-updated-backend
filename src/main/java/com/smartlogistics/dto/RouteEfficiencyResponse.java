package com.smartlogistics.dto;

public class RouteEfficiencyResponse {

    private final String routeName;
    private final double distance;
    private final double predictedFuel;
    private final double carbonEmission;
    private final double costPerKm;

    public RouteEfficiencyResponse(String routeName, double distance, double predictedFuel,
                                   double carbonEmission, double costPerKm) {
        this.routeName = routeName;
        this.distance = distance;
        this.predictedFuel = predictedFuel;
        this.carbonEmission = carbonEmission;
        this.costPerKm = costPerKm;
    }

    public String getRouteName() {
        return routeName;
    }

    public double getDistance() {
        return distance;
    }

    public double getPredictedFuel() {
        return predictedFuel;
    }

    public double getCarbonEmission() {
        return carbonEmission;
    }

    public double getCostPerKm() {
        return costPerKm;
    }
}
