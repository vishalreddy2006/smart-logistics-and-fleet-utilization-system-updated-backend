package com.smartlogistics.dto;

public class CostSummaryResponse {

    private final double totalRevenue;
    private final double totalOperationalCost;
    private final double netProfit;
    private final double averageCostPerKm;

    public CostSummaryResponse(double totalRevenue, double totalOperationalCost,
                               double netProfit, double averageCostPerKm) {
        this.totalRevenue = totalRevenue;
        this.totalOperationalCost = totalOperationalCost;
        this.netProfit = netProfit;
        this.averageCostPerKm = averageCostPerKm;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalOperationalCost() {
        return totalOperationalCost;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public double getAverageCostPerKm() {
        return averageCostPerKm;
    }
}
