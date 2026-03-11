package com.quantlib.curves;

/**
 * Interface representing a discount curve P(T; t=0).
 * Provides discount factors and derived forward rates.
 */
public interface DiscountCurve {

    /**
     * Returns the discount factor P(T; 0) for a given maturity T.
     */
    double getDiscountFactor(double maturity);

    /**
     * Returns the simply-compounded forward rate L(T1, T2; 0):
     *   L = (P(T1) / P(T2) - 1) / (T2 - T1)
     */
    default double getForwardRate(double periodStart, double periodEnd) {
        return (getDiscountFactor(periodStart) / getDiscountFactor(periodEnd) - 1.0)
                / (periodEnd - periodStart);
    }
}
