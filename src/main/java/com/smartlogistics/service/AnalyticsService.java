package com.smartlogistics.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.smartlogistics.entity.Trip;
import com.smartlogistics.entity.Vehicle;
import com.smartlogistics.repository.TripRepository;
import com.smartlogistics.repository.VehicleRepository;
import com.smartlogistics.util.UserContext;

@Service
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final UserContext userContext;

    public AnalyticsService(TripRepository tripRepository,
            VehicleRepository vehicleRepository,
            UserContext userContext) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.userContext = userContext;
    }

    // ─── Summary endpoint (/api/analytics) ───────────────────────────────────

    public Map<String, Object> getSummary() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();
        List<Vehicle> vehicles = userId != null ? vehicleRepository.findByUser_Id(userId) : List.of();

        int totalTrips = trips.size();
        double totalDistance = trips.stream().mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0).sum();
        double totalFuel = trips.stream().mapToDouble(Trip::getPredictedFuel).sum();
        double totalCO2 = trips.stream().mapToDouble(Trip::getCarbonEmission).sum();

        long availableVehicles = vehicles.stream()
                .filter(v -> "ACTIVE".equalsIgnoreCase(v.getStatus()) || "AVAILABLE".equalsIgnoreCase(v.getStatus()))
                .count();

        long maintenanceAlerts = vehicles.stream()
                .filter(v -> "HIGH".equalsIgnoreCase(v.getMaintenanceRisk()))
                .count();

        double revenue = totalDistance * 45.0;
        double fuelCost = totalFuel * 95.0;
        double maintenanceCost = maintenanceAlerts * 5000.0;
        double totalCost = fuelCost + maintenanceCost;
        double netProfit = revenue - totalCost;

        double efficiencyScore = totalFuel > 0 ? Math.min(100.0, (totalDistance / totalFuel) * 5.0) : 0.0;

        // Chart data grouped by real created_at date
        Map<LocalDate, long[]> byDate = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            byDate.put(today.minusDays(i), new long[]{0, 0}); // [tripCount, fuel*100]
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
        for (Trip t : trips) {
            LocalDate date = t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : today;
            if (byDate.containsKey(date)) {
                byDate.get(date)[0]++;
                byDate.get(date)[1] += Math.round(t.getPredictedFuel() * 100);
            }
        }

        List<String> dates = new ArrayList<>();
        List<Integer> tripsPerDay = new ArrayList<>();
        List<Double> fuelTrend = new ArrayList<>();

        for (Map.Entry<LocalDate, long[]> e : byDate.entrySet()) {
            dates.add(e.getKey().format(fmt));
            tripsPerDay.add((int) e.getValue()[0]);
            fuelTrend.add(e.getValue()[1] / 100.0);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTrips", totalTrips);
        result.put("totalDistance", round2(totalDistance));
        result.put("totalFuel", round2(totalFuel));
        result.put("totalCO2", round2(totalCO2));
        result.put("availableVehicles", availableVehicles);
        result.put("maintenanceAlerts", maintenanceAlerts);
        result.put("revenue", round2(revenue));
        result.put("fuelCost", round2(fuelCost));
        result.put("maintenanceCost", round2(maintenanceCost));
        result.put("totalCost", round2(totalCost));
        result.put("netProfit", round2(netProfit));
        result.put("efficiencyScore", round2(efficiencyScore));
        result.put("dates", dates);
        result.put("tripsPerDay", tripsPerDay);
        result.put("fuelTrend", fuelTrend);
        return result;
    }

    // ─── Existing endpoints (preserved) ──────────────────────────────────────

    public Map<String, Object> getTripActivity() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();

        int totalTrips = trips.size();
        double averageTripDistance = trips.stream()
                .filter(t -> t.getDistance() != null)
                .mapToDouble(Trip::getDistance)
                .average().orElse(0.0);

        List<Map<String, Object>> tripsPerDay = buildDateTrend(trips, "tripCount");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTripsToday", totalTrips);
        result.put("averageTripDistance", round2(averageTripDistance));
        result.put("tripsPerDay", tripsPerDay);
        return result;
    }

    public Map<String, Object> getFuelUsage() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();

        double totalFuelConsumed = trips.stream().mapToDouble(Trip::getPredictedFuel).sum();
        double averageFuelPerTrip = trips.isEmpty() ? 0.0 : totalFuelConsumed / trips.size();

        List<Map<String, Object>> fuelUsageTrend = buildDateTrend(trips, "fuel");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalFuelConsumed", round2(totalFuelConsumed));
        result.put("averageFuelPerTrip", round2(averageFuelPerTrip));
        result.put("fuelUsageTrend", fuelUsageTrend);
        return result;
    }

    public Map<String, Object> getProfitAnalysis() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();
        List<Vehicle> vehicles = userId != null ? vehicleRepository.findByUser_Id(userId) : List.of();

        double totalDistance = trips.stream().mapToDouble(t -> t.getDistance() != null ? t.getDistance() : 0.0).sum();
        double totalFuel = trips.stream().mapToDouble(Trip::getPredictedFuel).sum();
        long highRiskCount = vehicles.stream().filter(v -> "HIGH".equalsIgnoreCase(v.getMaintenanceRisk())).count();

        double revenue = totalDistance * 45.0;
        double fuelCost = totalFuel * 95.0;
        double maintenanceCost = highRiskCount * 5000.0;
        double totalCost = fuelCost + maintenanceCost;
        double netProfit = revenue - totalCost;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", round2(revenue));
        result.put("totalCost", round2(totalCost));
        result.put("netProfit", round2(netProfit));
        return result;
    }

    public Map<String, Object> getCarbonEmissions() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();

        double totalEmission = trips.stream().mapToDouble(Trip::getCarbonEmission).sum();
        double averageEmissionPerTrip = trips.isEmpty() ? 0.0 : totalEmission / trips.size();

        List<Map<String, Object>> emissionTrend = buildDateTrend(trips, "co2");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEmission", round2(totalEmission));
        result.put("averageEmissionPerTrip", round2(averageEmissionPerTrip));
        result.put("emissionTrend", emissionTrend);
        return result;
    }

    public Map<String, Object> getFleetUtilization() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Vehicle> vehicles = userId != null ? vehicleRepository.findByUser_Id(userId) : List.of();

        int availableVehicles = 0, vehiclesInTrip = 0, vehiclesUnderMaintenance = 0;
        for (Vehicle v : vehicles) {
            String status = v.getStatus() == null ? "" : v.getStatus().trim().toUpperCase();
            switch (status) {
                case "AVAILABLE", "ACTIVE" -> availableVehicles++;
                case "IN_TRIP", "IN_USE", "BUSY" -> vehiclesInTrip++;
                case "MAINTENANCE", "UNDER_MAINTENANCE" -> vehiclesUnderMaintenance++;
                default -> {}
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("availableVehicles", availableVehicles);
        result.put("vehiclesInTrip", vehiclesInTrip);
        result.put("vehiclesUnderMaintenance", vehiclesUnderMaintenance);
        return result;
    }

    public Map<String, Object> getVehiclePerformance() {
        Long userId = userContext.getCurrentUserIdOrNull();
        List<Trip> trips = userId != null ? tripRepository.findByUserId(userId) : List.of();
        List<Vehicle> vehicles = userId != null ? vehicleRepository.findByUser_Id(userId) : List.of();

        Map<Long, Vehicle> vehiclesById = new HashMap<>();
        for (Vehicle v : vehicles) vehiclesById.put(v.getVehicleId(), v);

        Map<Long, Integer> tripCount = new HashMap<>();
        Map<Long, Double> fuelByVehicle = new HashMap<>();
        for (Trip t : trips) {
            if (t.getVehicle() == null || t.getVehicle().getVehicleId() == null) continue;
            Long vid = t.getVehicle().getVehicleId();
            tripCount.merge(vid, 1, Integer::sum);
            fuelByVehicle.merge(vid, t.getPredictedFuel(), Double::sum);
        }

        List<Map<String, Object>> tripCountPerVehicle = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : tripCount.entrySet()) {
            Long vid = e.getKey();
            Vehicle v = vehiclesById.get(vid);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("vehicleId", vid);
            item.put("vehicleType", v != null ? v.getVehicleType() : "Unknown");
            item.put("tripCount", e.getValue());
            item.put("fuelUsed", round2(fuelByVehicle.getOrDefault(vid, 0.0)));
            item.put("maintenanceRisk", v != null ? v.getMaintenanceRisk() : null);
            tripCountPerVehicle.add(item);
        }
        tripCountPerVehicle.sort(Comparator.comparingInt(a -> -((Number) a.get("tripCount")).intValue()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topUsedVehicles", tripCountPerVehicle.stream().limit(5).toList());
        result.put("tripCountPerVehicle", tripCountPerVehicle);
        return result;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private List<Map<String, Object>> buildDateTrend(List<Trip> trips, String mode) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
        LocalDate today = LocalDate.now();
        Map<LocalDate, double[]> buckets = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) buckets.put(today.minusDays(i), new double[]{0});

        for (Trip t : trips) {
            LocalDate date = t.getCreatedAt() != null ? t.getCreatedAt().toLocalDate() : today;
            if (!buckets.containsKey(date)) continue;
            switch (mode) {
                case "tripCount" -> buckets.get(date)[0]++;
                case "fuel" -> buckets.get(date)[0] += t.getPredictedFuel();
                case "co2" -> buckets.get(date)[0] += t.getCarbonEmission();
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> e : buckets.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("label", e.getKey().format(fmt));
            item.put("value", "tripCount".equals(mode) ? (int) e.getValue()[0] : round2(e.getValue()[0]));
            trend.add(item);
        }
        return trend;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
