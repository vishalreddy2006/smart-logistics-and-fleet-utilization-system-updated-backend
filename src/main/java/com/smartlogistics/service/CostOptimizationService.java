package com.smartlogistics.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.smartlogistics.dto.CostRecommendationResponse;
import com.smartlogistics.dto.CostSummaryResponse;
import com.smartlogistics.dto.RouteEfficiencyResponse;
import com.smartlogistics.dto.VehicleEfficiencyResponse;
import com.smartlogistics.entity.Trip;
import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.repository.TripRepository;
import com.smartlogistics.repository.VehicleRepository;

@Service
public class CostOptimizationService {

    private static final double FUEL_PRICE_PER_LITRE = 105.0;
    private static final double MAINTENANCE_RATE_PER_KM = 2.5;
    private static final double REVENUE_RATE_PER_KM = 45.0;
    private static final double REVENUE_RATE_PER_KG = 1.2;

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;

    public CostOptimizationService(TripRepository tripRepository, VehicleRepository vehicleRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public CostSummaryResponse getSummary(Long userId) {
        List<Trip> trips = tripRepository.findByUserId(userId);

        double totalRevenue = 0.0;
        double totalOperationalCost = 0.0;
        double totalDistance = 0.0;

        for (Trip trip : trips) {
            Double rawDistance = trip.getDistance();
            Double rawLoadWeight = trip.getLoadWeight();
            double distance = rawDistance != null ? rawDistance : 0.0;
            double loadWeight = rawLoadWeight != null ? rawLoadWeight : 0.0;
            double fuel = trip.getPredictedFuel();

            double tripRevenue = (distance * REVENUE_RATE_PER_KM) + (loadWeight * REVENUE_RATE_PER_KG);
            double fuelCost = fuel * FUEL_PRICE_PER_LITRE;
            double maintenanceCost = distance * MAINTENANCE_RATE_PER_KM;

            totalRevenue += tripRevenue;
            totalOperationalCost += fuelCost + maintenanceCost;
            totalDistance += distance;
        }

        double netProfit = totalRevenue - totalOperationalCost;
        double averageCostPerKm = totalDistance > 0 ? totalOperationalCost / totalDistance : 0.0;

        return new CostSummaryResponse(
                round2(totalRevenue),
                round2(totalOperationalCost),
                round2(netProfit),
                round2(averageCostPerKm)
        );
    }

    public List<VehicleEfficiencyResponse> getVehicleEfficiency(Long userId) {
        List<Trip> trips = tripRepository.findByUserId(userId);
        List<Vehicle> vehicles = vehicleRepository.findByUser_Id(userId);

        Map<Long, Vehicle> vehicleMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleMap.put(v.getVehicleId(), v);
        }

        // Aggregate per-vehicle stats
        Map<Long, double[]> statsById = new LinkedHashMap<>();
        // double[] = [totalCost, totalDistance, totalFuel, tripCount]

        for (Trip trip : trips) {
            if (trip.getVehicle() == null || trip.getVehicle().getVehicleId() == null) {
                continue;
            }

            Long vehicleId = trip.getVehicle().getVehicleId();
            Double rawDist = trip.getDistance();
            double distance = rawDist != null ? rawDist : 0.0;
            double fuel = trip.getPredictedFuel();
            double fuelCost = fuel * FUEL_PRICE_PER_LITRE;
            double maintenanceCost = distance * MAINTENANCE_RATE_PER_KM;
            double totalCost = fuelCost + maintenanceCost;

            double[] stats = statsById.computeIfAbsent(vehicleId, id -> new double[4]);
            stats[0] += totalCost;
            stats[1] += distance;
            stats[2] += fuel;
            stats[3] += 1;
        }

        List<VehicleEfficiencyResponse> result = new ArrayList<>();

        for (Map.Entry<Long, double[]> entry : statsById.entrySet()) {
            Long vehicleId = entry.getKey();
            double[] stats = entry.getValue();
            Vehicle vehicle = vehicleMap.get(vehicleId);

            double totalCost = stats[0];
            double totalDistance = stats[1];
            double totalFuel = stats[2];
            int tripCount = (int) stats[3];

            double costPerKm = totalDistance > 0 ? totalCost / totalDistance : 0.0;
            double fuelEfficiency = totalFuel > 0 ? totalDistance / totalFuel : 0.0;

            String vehicleType = vehicle != null ? vehicle.getVehicleType() : "Unknown";

            result.add(new VehicleEfficiencyResponse(
                    vehicleId,
                    vehicleType,
                    round2(costPerKm),
                    round2(fuelEfficiency),
                    tripCount
            ));
        }

        result.sort(Comparator.comparingDouble(VehicleEfficiencyResponse::getCostPerKm));
        return result;
    }

    public List<RouteEfficiencyResponse> getRouteEfficiency(Long userId) {
        List<Trip> trips = tripRepository.findByUserId(userId);

        // Group by source → destination
        Map<String, double[]> routeStats = new LinkedHashMap<>();
        // double[] = [totalDistance, totalFuel, totalCarbon, tripCount]

        for (Trip trip : trips) {
            String source = trip.getSource() == null ? "Unknown" : trip.getSource().trim();
            String destination = trip.getDestination() == null ? "Unknown" : trip.getDestination().trim();
            String routeKey = source + " → " + destination;

            Double rawDist2 = trip.getDistance();
            double distance = rawDist2 != null ? rawDist2 : 0.0;
            double fuel = trip.getPredictedFuel();
            double carbon = trip.getCarbonEmission();

            double[] stats = routeStats.computeIfAbsent(routeKey, k -> new double[4]);
            stats[0] += distance;
            stats[1] += fuel;
            stats[2] += carbon;
            stats[3] += 1;
        }

        List<RouteEfficiencyResponse> result = new ArrayList<>();

        for (Map.Entry<String, double[]> entry : routeStats.entrySet()) {
            String routeName = entry.getKey();
            double[] stats = entry.getValue();
            double tripCount = stats[3];

            double avgDistance = tripCount > 0 ? stats[0] / tripCount : 0.0;
            double avgFuel = tripCount > 0 ? stats[1] / tripCount : 0.0;
            double avgCarbon = tripCount > 0 ? stats[2] / tripCount : 0.0;

            double fuelCost = avgFuel * FUEL_PRICE_PER_LITRE;
            double maintenanceCost = avgDistance * MAINTENANCE_RATE_PER_KM;
            double totalCost = fuelCost + maintenanceCost;
            double costPerKm = avgDistance > 0 ? totalCost / avgDistance : 0.0;

            result.add(new RouteEfficiencyResponse(
                    routeName,
                    round2(avgDistance),
                    round2(avgFuel),
                    round2(avgCarbon),
                    round2(costPerKm)
            ));
        }

        result.sort(Comparator.comparingDouble(RouteEfficiencyResponse::getCostPerKm));
        return result;
    }

    public List<CostRecommendationResponse> getRecommendations(Long userId) {
        List<Trip> trips = tripRepository.findByUserId(userId);
        List<Vehicle> vehicles = vehicleRepository.findByUser_Id(userId);
        List<CostRecommendationResponse> recommendations = new ArrayList<>();

        if (trips.isEmpty()) {
            recommendations.add(new CostRecommendationResponse(
                    "INFO", "No Trip Data", "No trips found. Add trips to receive optimization recommendations."
            ));
            return recommendations;
        }

        // Fleet average fuel per km
        double totalFuel = 0.0;
        double totalDistance = 0.0;
        for (Trip trip : trips) {
            totalFuel += trip.getPredictedFuel();
            Double rawD = trip.getDistance();
            totalDistance += rawD != null ? rawD : 0.0;
        }
        double fleetAvgFuelPerKm = totalDistance > 0 ? totalFuel / totalDistance : 0.0;

        // Per-vehicle fuel consumption stats
        Map<Long, double[]> vehicleFuelStats = new HashMap<>();
        // double[] = [totalFuel, totalDistance, tripCount, totalCost]
        for (Trip trip : trips) {
            if (trip.getVehicle() == null || trip.getVehicle().getVehicleId() == null) {
                continue;
            }
            Long vehicleId = trip.getVehicle().getVehicleId();
            Double rawDistV = trip.getDistance();
            double distance = rawDistV != null ? rawDistV : 0.0;
            double fuel = trip.getPredictedFuel();
            double fuelCost = fuel * FUEL_PRICE_PER_LITRE;
            double maintenanceCost = distance * MAINTENANCE_RATE_PER_KM;

            double[] stats = vehicleFuelStats.computeIfAbsent(vehicleId, id -> new double[4]);
            stats[0] += fuel;
            stats[1] += distance;
            stats[2] += 1;
            stats[3] += fuelCost + maintenanceCost;
        }

        // Highest cost vehicle
        vehicleFuelStats.entrySet().stream()
                .max(Comparator.comparingDouble(e -> e.getValue()[3]))
                .ifPresent(entry -> {
                    Long vehicleId = entry.getKey();
                    double totalCostForVehicle = entry.getValue()[3];
                    recommendations.add(new CostRecommendationResponse(
                            "WARNING",
                            "Highest Operational Cost Vehicle",
                            String.format("Vehicle #%d has the highest operational cost of Rs %.0f. Consider reviewing its route assignments.", vehicleId, totalCostForVehicle)
                    ));
                });

        // Vehicles consuming more fuel than fleet average
        Map<Long, Vehicle> vehicleMap = new HashMap<>();
        for (Vehicle v : vehicles) {
            vehicleMap.put(v.getVehicleId(), v);
        }

        for (Map.Entry<Long, double[]> entry : vehicleFuelStats.entrySet()) {
            double[] stats = entry.getValue();
            double vehicleDistance = stats[1];
            double vehicleFuel = stats[0];
            double vehicleFuelPerKm = vehicleDistance > 0 ? vehicleFuel / vehicleDistance : 0.0;

            if (fleetAvgFuelPerKm > 0 && vehicleFuelPerKm > fleetAvgFuelPerKm * 1.2) {
                Long vehicleId = entry.getKey();
                Vehicle vehicle = vehicleMap.get(vehicleId);
                String vehicleType = vehicle != null ? vehicle.getVehicleType() : "Unknown";
                recommendations.add(new CostRecommendationResponse(
                        "WARNING",
                        "Above-Average Fuel Consumption",
                        String.format("Vehicle #%d (%s) consumes %.2f L/km, which is 20%% above the fleet average of %.2f L/km.",
                                vehicleId, vehicleType, vehicleFuelPerKm, fleetAvgFuelPerKm)
                ));
            }
        }

        // Most fuel-efficient vehicle
        vehicleFuelStats.entrySet().stream()
                .filter(e -> e.getValue()[1] > 0)
                .min(Comparator.comparingDouble(e -> e.getValue()[0] / e.getValue()[1]))
                .ifPresent(entry -> {
                    Long vehicleId = entry.getKey();
                    double[] stats = entry.getValue();
                    double fuelPerKm = stats[0] / stats[1];
                    Vehicle vehicle = vehicleMap.get(vehicleId);
                    String vehicleType = vehicle != null ? vehicle.getVehicleType() : "Unknown";
                    recommendations.add(new CostRecommendationResponse(
                            "SUCCESS",
                            "Most Fuel Efficient Vehicle",
                            String.format("Vehicle #%d (%s) is the most fuel-efficient at %.2f L/km. Prioritize it for long-distance routes.", vehicleId, vehicleType, fuelPerKm)
                    ));
                });

        // Vehicles with HIGH maintenance risk
        for (Vehicle vehicle : vehicles) {
            if ("HIGH".equalsIgnoreCase(vehicle.getMaintenanceRisk())) {
                recommendations.add(new CostRecommendationResponse(
                        "WARNING",
                        "High Maintenance Risk Vehicle",
                        String.format("Vehicle #%d (%s) has HIGH maintenance risk. Schedule maintenance to prevent cost escalation.", vehicle.getVehicleId(), vehicle.getVehicleType())
                ));
            }
        }

        // Best route for cost efficiency
        List<RouteEfficiencyResponse> routes = getRouteEfficiency(userId);
        if (!routes.isEmpty()) {
            RouteEfficiencyResponse bestRoute = routes.get(0);
            recommendations.add(new CostRecommendationResponse(
                    "INFO",
                    "Best Route for Cost Efficiency",
                    String.format("Route '%s' has the lowest cost per km (Rs %.2f/km). Prioritise this route to reduce operational costs.", bestRoute.getRouteName(), bestRoute.getCostPerKm())
            ));

            if (routes.size() > 1) {
                RouteEfficiencyResponse worstRoute = routes.get(routes.size() - 1);
                recommendations.add(new CostRecommendationResponse(
                        "WARNING",
                        "High Cost Route Detected",
                        String.format("Route '%s' costs Rs %.2f/km. Review load allocation or consider alternate routes.", worstRoute.getRouteName(), worstRoute.getCostPerKm())
                ));
            }
        }

        return recommendations;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
