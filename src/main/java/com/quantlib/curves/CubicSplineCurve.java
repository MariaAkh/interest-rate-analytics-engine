package com.quantlib.curves;

/**
 * Discount curve using natural cubic spline interpolation.
 * Produces a smooth C2-continuous curve — preferred for forward rate stability.
 */
public class CubicSplineCurve implements DiscountCurve {

    private final double[] maturities;
    private final double[] discountFactors;
    private final double[] coeffB, coeffC, coeffD;

    public CubicSplineCurve(double[] maturities, double[] discountFactors) {
        if (maturities.length != discountFactors.length || maturities.length < 2)
            throw new IllegalArgumentException("Need at least 2 matching data points.");
        this.maturities = maturities.clone();
        this.discountFactors = discountFactors.clone();
        int n = maturities.length;
        coeffB = new double[n];
        coeffC = new double[n];
        coeffD = new double[n];
        computeSpline(n);
    }

    private void computeSpline(int n) {
        double[] h = new double[n - 1];
        double[] alpha = new double[n];
        for (int i = 0; i < n - 1; i++) h[i] = maturities[i + 1] - maturities[i];
        for (int i = 1; i < n - 1; i++)
            alpha[i] = (3.0 / h[i]) * (discountFactors[i + 1] - discountFactors[i])
                     - (3.0 / h[i - 1]) * (discountFactors[i] - discountFactors[i - 1]);

        double[] l = new double[n], mu = new double[n], z = new double[n];
        l[0] = 1.0;
        for (int i = 1; i < n - 1; i++) {
            l[i] = 2.0 * (maturities[i + 1] - maturities[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }
        l[n - 1] = 1.0;
        for (int j = n - 2; j >= 0; j--) {
            coeffC[j] = z[j] - mu[j] * coeffC[j + 1];
            coeffB[j] = (discountFactors[j + 1] - discountFactors[j]) / h[j]
                      - h[j] * (coeffC[j + 1] + 2.0 * coeffC[j]) / 3.0;
            coeffD[j] = (coeffC[j + 1] - coeffC[j]) / (3.0 * h[j]);
        }
    }

    @Override
    public double getDiscountFactor(double maturity) {
        int n = maturities.length;
        if (maturity <= maturities[0]) return discountFactors[0];
        if (maturity >= maturities[n - 1]) return discountFactors[n - 1];

        int i = n - 2;
        for (int j = 0; j < n - 1; j++) {
            if (maturity <= maturities[j + 1]) { i = j; break; }
        }
        double dx = maturity - maturities[i];
        return discountFactors[i]
             + coeffB[i] * dx
             + coeffC[i] * dx * dx
             + coeffD[i] * dx * dx * dx;
    }
}
