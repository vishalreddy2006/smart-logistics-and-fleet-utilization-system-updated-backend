package com.smartlogistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteService.class);

    @Value("${ors.api.key}")
    private String apiKey;

    private static final String ORS_URL =
        "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    private static final String[] PREFERENCES = {"recommended", "fastest", "shortest"};

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> getAlternativeRoutes(double srcLat, double srcLng,
                                                           double dstLat, double dstLng) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String preference : PREFERENCES) {
            Map<String, Object> route = callORS(srcLat, srcLng, dstLat, dstLng, preference);
            if (route != null) {
                results.add(route);
            }
        }
        return results;
    }

    private Map<String, Object> callORS(double srcLat, double srcLng,
                                         double dstLat, double dstLng,
                                         String preference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);
        headers.set("Accept", "application/json, application/geo+json");

        String body = String.format(Locale.US,
            "{\"coordinates\":[[%.6f,%.6f],[%.6f,%.6f]]," +
            "\"preference\":\"%s\"," +
            "\"units\":\"km\"," +
            "\"geometry\":true," +
            "\"instructions\":false}",
            srcLng, srcLat, dstLng, dstLat, preference
        );

        try {
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(ORS_URL, request, String.class);

            JsonNode root     = mapper.readTree(response.getBody());
            JsonNode features = root.path("features");

            if (!features.isArray() || features.size() == 0) {
                log.warn("ORS [{}] returned no features", preference);
                return null;
            }

            JsonNode feature    = features.get(0);
            JsonNode summary    = feature.path("properties").path("summary");
            JsonNode geomCoords = feature.path("geometry").path("coordinates");

            double distanceKm  = summary.path("distance").asDouble();
            double durationSec = summary.path("duration").asDouble();

            if (distanceKm <= 0) {
                log.warn("ORS [{}] returned zero distance", preference);
                return null;
            }

            List<List<Double>> coords = new ArrayList<>();
            for (JsonNode point : geomCoords) {
                coords.add(List.of(point.get(1).asDouble(), point.get(0).asDouble()));
            }

            if (coords.size() < 2) return null;

            return Map.of(
                "distance_km",    round2(distanceKm),
                "duration_hours", round2(durationSec / 3600.0),
                "coordinates",    coords,
                "preference",     preference
            );

        } catch (HttpClientErrorException e) {
            log.error("ORS [{}] HTTP error {}: {}", preference, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("ORS [{}] failed: {}", preference, e.getMessage());
        }

        return null;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
