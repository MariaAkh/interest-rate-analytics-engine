package com.quantlib.instruments;

import com.quantlib.curves.DiscountCurve;

/**
 * A plain vanilla interest rate swap (payer: receive float, pay fixed).
 *
 * Valuation formula:
 *   V = sum_i [ (L(T_i, T_{i+1}; 0) - K) * (T_{i+1} - T_i) * P(T_{i+1}; 0) ]
 *
 * Par swap rate K* satisfies V(K*) = 0.
 */
public class Swap {

    private final double[] tenorTimes;
    private final double fixedRate;

    public Swap(double[] tenorTimes, double fixedRate) {
        if (tenorTimes.length < 2)
            throw new IllegalArgumentException("Swap needs at least 2 tenor dates.");
        this.tenorTimes = tenorTimes.clone();
        this.fixedRate = fixedRate;
    }

    /**
     * Values the swap using the given discount curve.
     */
    public double getValue(DiscountCurve curve) {
        double value = 0.0;
        for (int i = 0; i < tenorTimes.length - 1; i++) {
            double dt = tenorTimes[i + 1] - tenorTimes[i];
            double forwardRate = curve.getForwardRate(tenorTimes[i], tenorTimes[i + 1]);
            double df = curve.getDiscountFactor(tenorTimes[i + 1]);
            value += (forwardRate - fixedRate) * dt * df;
        }
        return value;
    }

    /**
     * Computes the par swap rate: the unique fixed rate K* such that getValue(K*) == 0.
     *   K* = sum_i [L(T_i,T_{i+1}) * dt_i * P(T_{i+1})] / sum_i [dt_i * P(T_{i+1})]
     */
    public double getParSwapRate(DiscountCurve curve) {
        double numerator = 0.0;
        double annuity = 0.0;
        for (int i = 0; i < tenorTimes.length - 1; i++) {
            double dt = tenorTimes[i + 1] - tenorTimes[i];
            double forwardRate = curve.getForwardRate(tenorTimes[i], tenorTimes[i + 1]);
            double df = curve.getDiscountFactor(tenorTimes[i + 1]);
            numerator += forwardRate * dt * df;
            annuity   += dt * df;
        }
        return numerator / annuity;
    }

    /**
     * Returns the annuity (PV01 per unit notional) of the swap.
     * Used for computing DV01 and hedge ratios.
     */
    public double getAnnuity(DiscountCurve curve) {
        double annuity = 0.0;
        for (int i = 0; i < tenorTimes.length - 1; i++) {
            double dt = tenorTimes[i + 1] - tenorTimes[i];
            annuity += dt * curve.getDiscountFactor(tenorTimes[i + 1]);
        }
        return annuity;
    }

    public double getFixedRate() { return fixedRate; }
    public double[] getTenorTimes() { return tenorTimes.clone(); }
}
