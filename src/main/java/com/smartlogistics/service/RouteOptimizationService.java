package com.smartlogistics.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.smartlogistics.dto.RouteOptimizationResponse;
import com.smartlogistics.dto.RouteOptionResponse;

@Service
public class RouteOptimizationService {

    private static final Map<String, double[]> CITY_COORDS = Map.ofEntries(
        Map.entry("mumbai",     new double[]{19.0760, 72.8777}),
        Map.entry("delhi",      new double[]{28.6139, 77.2090}),
        Map.entry("bangalore",  new double[]{12.9716, 77.5946}),
        Map.entry("bengaluru",  new double[]{12.9716, 77.5946}),
        Map.entry("chennai",    new double[]{13.0827, 80.2707}),
        Map.entry("hyderabad",  new double[]{17.3850, 78.4867}),
        Map.entry("pune",       new double[]{18.5204, 73.8567}),
        Map.entry("kolkata",    new double[]{22.5726, 88.3639}),
        Map.entry("ahmedabad",  new double[]{23.0225, 72.5714}),
        Map.entry("jaipur",     new double[]{26.9124, 75.7873}),
        Map.entry("surat",      new double[]{21.1702, 72.8311}),
        Map.entry("lucknow",    new double[]{26.8467, 80.9462}),
        Map.entry("nagpur",     new double[]{21.1458, 79.0882}),
        Map.entry("indore",     new double[]{22.7196, 75.8577}),
        Map.entry("bhopal",     new double[]{23.2599, 77.4126}),
        Map.entry("patna",      new double[]{25.5941, 85.1376}),
        Map.entry("chandigarh", new double[]{30.7333, 76.7794}),
        Map.entry("coimbatore", new double[]{11.0168, 76.9558}),
        Map.entry("kochi",      new double[]{9.9312,  76.2673}),
        Map.entry("guwahati",   new double[]{26.1445, 91.7362})
    );

    private static final double VEHICLE_EFFICIENCY = 0.22; // L/km for a standard truck
    private static final double CO2_PER_LITRE      = 2.68; // kg CO2 per litre diesel

    // ORS preference → display name + type
    // Order matches RouteService.PREFERENCES = ["recommended","fastest","shortest"]
    private static final String[] ROUTE_NAMES = {"Balanced Route", "Express Route", "Eco Route"};
    private static final String[] ROUTE_TYPES = {"balanced", "express", "eco"};

    private final RouteService routeService;

    public RouteOptimizationService(RouteService routeService) {
        this.routeService = routeService;
    }

    public RouteOptimizationResponse optimizeRoutes(String source, String destination) {
        double[] src = CITY_COORDS.get(source.trim().toLowerCase());
        double[] dst = CITY_COORDS.get(destination.trim().toLowerCase());

        List<RouteOptionResponse> routes;

        if (src != null && dst != null) {
            List<Map<String, Object>> orsRoutes =
                routeService.getAlternativeRoutes(src[0], src[1], dst[0], dst[1]);
            routes = orsRoutes.isEmpty()
                ? fallbackRoutes(src, dst)
                : buildRoutes(orsRoutes);
        } else {
            double[] s = src != null ? src : new double[]{20.0, 78.0};
            double[] d = dst != null ? dst : new double[]{22.0, 80.0};
            routes = fallbackRoutes(s, d);
        }

        // Normalize raw scores → 0–100 (higher score = better route)
        double minRaw = routes.stream().mapToDouble(RouteOptionResponse::getRouteScore).min().orElse(1);
        double maxRaw = routes.stream().mapToDouble(RouteOptionResponse::getRouteScore).max().orElse(1);
        routes.forEach(r -> {
            double normalized = (maxRaw == minRaw) ? 50.0
                : 100.0 - ((r.getRouteScore() - minRaw) / (maxRaw - minRaw) * 100.0);
            r.setRouteScore(round2(normalized));
        });

        RouteOptionResponse recommended = routes.stream()
            .max(Comparator.comparingDouble(RouteOptionResponse::getRouteScore))
            .orElse(routes.get(0));

        return new RouteOptimizationResponse(routes, recommended);
    }

    @SuppressWarnings("unchecked")
    private List<RouteOptionResponse> buildRoutes(List<Map<String, Object>> orsRoutes) {
        List<RouteOptionResponse> result = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            // If ORS returned fewer than 3, reuse last available route
            Map<String, Object> r = orsRoutes.get(Math.min(i, orsRoutes.size() - 1));

            double distKm      = (double) r.get("distance_km");
            double durationHrs = (double) r.get("duration_hours");
            List<List<Double>> coords = (List<List<Double>>) r.get("coordinates");

            double fuel     = round2(distKm * VEHICLE_EFFICIENCY);
            double co2      = round2(fuel * CO2_PER_LITRE);
            // Raw score: lower = worse (more cost). 50% time + 30% fuel + 20% co2
            double rawScore = round2(0.5 * durationHrs + 0.3 * fuel + 0.2 * co2);

            result.add(new RouteOptionResponse(
                ROUTE_NAMES[i], ROUTE_TYPES[i],
                distKm, durationHrs, fuel, co2, rawScore,
                coords != null ? coords : List.of()
            ));
        }

        return result;
    }

    /** Fallback when ORS is unreachable — uses haversine × road factor, no map geometry */
    private List<RouteOptionResponse> fallbackRoutes(double[] src, double[] dst) {
        double baseDist = haversine(src[0], src[1], dst[0], dst[1]);

        // Indian roads: road distance ≈ 1.25–1.35× straight-line distance
        double[] distMult = {1.30, 1.35, 1.25};
        double[] speedKmh = {58.0, 65.0, 55.0};
        String[] names    = {"Balanced Route", "Express Route", "Eco Route"};
        String[] types    = {"balanced", "express", "eco"};

        List<RouteOptionResponse> routes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double dist = round2(baseDist * distMult[i]);
            double time = round2(dist / speedKmh[i]);
            double fuel = round2(dist * VEHICLE_EFFICIENCY);
            double co2  = round2(fuel * CO2_PER_LITRE);
            double raw  = round2(0.5 * time + 0.3 * fuel + 0.2 * co2);
            routes.add(new RouteOptionResponse(names[i], types[i], dist, time, fuel, co2, raw, List.of()));
        }
        return routes;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
