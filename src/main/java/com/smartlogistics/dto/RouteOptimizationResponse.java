package com.smartlogistics.dto;

import java.util.List;

public class RouteOptimizationResponse {

    private List<RouteOptionResponse> routes;
    private RouteOptionResponse recommendedRoute;

    public RouteOptimizationResponse() {
    }

    public RouteOptimizationResponse(List<RouteOptionResponse> routes, RouteOptionResponse recommendedRoute) {
        this.routes = routes;
        this.recommendedRoute = recommendedRoute;
    }

    public List<RouteOptionResponse> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteOptionResponse> routes) {
        this.routes = routes;
    }

    public RouteOptionResponse getRecommendedRoute() {
        return recommendedRoute;
    }

    public void setRecommendedRoute(RouteOptionResponse recommendedRoute) {
        this.recommendedRoute = recommendedRoute;
    }
}
