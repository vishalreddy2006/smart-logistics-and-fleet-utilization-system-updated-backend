package com.smartlogistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogistics.dto.CostRecommendationResponse;
import com.smartlogistics.dto.CostSummaryResponse;
import com.smartlogistics.dto.RouteEfficiencyResponse;
import com.smartlogistics.dto.VehicleEfficiencyResponse;
import com.smartlogistics.service.CostOptimizationService;
import com.smartlogistics.util.UserContext;

@RestController
@RequestMapping("/api/cost-optimization")
public class CostOptimizationController {

    private final CostOptimizationService costOptimizationService;
    private final UserContext userContext;

    public CostOptimizationController(CostOptimizationService costOptimizationService, UserContext userContext) {
        this.costOptimizationService = costOptimizationService;
        this.userContext = userContext;
    }

    @GetMapping("/summary")
    public ResponseEntity<CostSummaryResponse> getSummary() {
        return ResponseEntity.ok(costOptimizationService.getSummary(userContext.getCurrentUserId()));
    }

    @GetMapping("/vehicle-efficiency")
    public ResponseEntity<List<VehicleEfficiencyResponse>> getVehicleEfficiency() {
        return ResponseEntity.ok(costOptimizationService.getVehicleEfficiency(userContext.getCurrentUserId()));
    }

    @GetMapping("/route-efficiency")
    public ResponseEntity<List<RouteEfficiencyResponse>> getRouteEfficiency() {
        return ResponseEntity.ok(costOptimizationService.getRouteEfficiency(userContext.getCurrentUserId()));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<CostRecommendationResponse>> getRecommendations() {
        return ResponseEntity.ok(costOptimizationService.getRecommendations(userContext.getCurrentUserId()));
    }
}
