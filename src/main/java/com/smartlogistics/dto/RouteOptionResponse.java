package com.smartlogistics.dto;

import java.util.List;

public class RouteOptionResponse {

    private String routeName;
    private String routeType;
    private double distance;
    private double estimatedTime;
    private double predictedFuel;
    private double carbonEmission;
    private double routeScore;
    private List<List<Double>> coordinates;

    public RouteOptionResponse() {
    }

    public RouteOptionResponse(
            String routeName,
            String routeType,
            double distance,
            double estimatedTime,
            double predictedFuel,
            double carbonEmission,
            double routeScore,
            List<List<Double>> coordinates
    ) {
        this.routeName = routeName;
        this.routeType = routeType;
        this.distance = distance;
        this.estimatedTime = estimatedTime;
        this.predictedFuel = predictedFuel;
        this.carbonEmission = carbonEmission;
        this.routeScore = routeScore;
        this.coordinates = coordinates;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
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

    public double getRouteScore() {
        return routeScore;
    }

    public void setRouteScore(double routeScore) {
        this.routeScore = routeScore;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
}
