package com.smartlogistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogistics.dto.RouteOptimizationResponse;
import com.smartlogistics.service.RouteOptimizationService;

@RestController
@RequestMapping("/api/routes")
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    public RouteOptimizationController(RouteOptimizationService routeOptimizationService) {
        this.routeOptimizationService = routeOptimizationService;
    }

    @GetMapping("/optimize")
    public ResponseEntity<RouteOptimizationResponse> optimizeRoutes(
            @RequestParam String source,
            @RequestParam String destination) {

        if (source == null || source.isBlank() || destination == null || destination.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(routeOptimizationService.optimizeRoutes(source, destination));
    }
}
