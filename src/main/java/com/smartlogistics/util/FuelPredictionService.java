package com.smartlogistics.util;

import org.springframework.stereotype.Service;

/**
 * Linear Regression fuel prediction model.
 * fuel = β0 + β1*distance + β2*load + β3*vehicleAge + β4*(distance/mileage)
 */
@Service
public class FuelPredictionService {

    private static final double B0 = 5.0;
    private static final double B1 = 0.18;
    private static final double B2 = 2.5;
    private static final double B3 = 1.2;
    private static final double B4 = 0.6;

    public double predictFuel(double distance, double load, int vehicleAge, double mileage) {
        double safeMileage = mileage > 0 ? mileage : 1.0;
        double baseFuel = distance / safeMileage;

        double fuel = B0
                + (B1 * distance)
                + (B2 * load)
                + (B3 * vehicleAge)
                + (B4 * baseFuel);

        return Math.max(fuel, baseFuel);
    }

    public double predictFuel(double distance, double load, int vehicleAge) {
        return predictFuel(distance, load, vehicleAge, 5.0);
    }
}
