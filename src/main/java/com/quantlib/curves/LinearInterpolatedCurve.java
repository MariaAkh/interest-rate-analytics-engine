package com.quantlib.curves;

import java.util.Arrays;

/**
 * Discount curve using piecewise linear interpolation between knot points.
 * Simple and fast; used as a baseline interpolation method.
 */
public class LinearInterpolatedCurve implements DiscountCurve {

    private final double[] maturities;
    private final double[] discountFactors;

    public LinearInterpolatedCurve(double[] maturities, double[] discountFactors) {
        if (maturities.length != discountFactors.length || maturities.length < 2)
            throw new IllegalArgumentException("Need at least 2 matching data points.");
        this.maturities = Arrays.copyOf(maturities, maturities.length);
        this.discountFactors = Arrays.copyOf(discountFactors, discountFactors.length);
    }

    @Override
    public double getDiscountFactor(double maturity) {
        if (maturity <= maturities[0]) return discountFactors[0];
        if (maturity >= maturities[maturities.length - 1])
            return discountFactors[discountFactors.length - 1];

        int i = Arrays.binarySearch(maturities, maturity);
        if (i >= 0) return discountFactors[i];

        int upper = -i - 1;
        int lower = upper - 1;
        double t = (maturity - maturities[lower]) / (maturities[upper] - maturities[lower]);
        return discountFactors[lower] + t * (discountFactors[upper] - discountFactors[lower]);
    }
}
