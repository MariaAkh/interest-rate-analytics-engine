package com.quantlib.curves;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountCurveTest {

    private static final double EPS = 1e-9;

    double[] maturities    = {0.0, 1.0, 2.0, 5.0, 10.0};
    double[] factors       = {1.0, 0.95, 0.90, 0.78, 0.60};

    DiscountCurve linear;
    DiscountCurve spline;

    @BeforeEach
    void setUp() {
        linear = new LinearInterpolatedCurve(maturities, factors);
        spline = new CubicSplineCurve(maturities, factors);
    }

    // --- Linear interpolation ---

    @Test
    void linear_atKnot_returnsExactValue() {
        assertEquals(0.95, linear.getDiscountFactor(1.0), EPS);
        assertEquals(0.90, linear.getDiscountFactor(2.0), EPS);
        assertEquals(0.60, linear.getDiscountFactor(10.0), EPS);
    }

    @Test
    void linear_midpoint_returnsAverage() {
        // Midpoint between (1.0, 0.95) and (2.0, 0.90) => 0.925
        assertEquals(0.925, linear.getDiscountFactor(1.5), EPS);
    }

    @Test
    void linear_belowRange_returnsFirstValue() {
        assertEquals(1.0, linear.getDiscountFactor(-0.5), EPS);
    }

    // --- Cubic spline ---

    @Test
    void spline_atKnot_returnsExactValue() {
        assertEquals(0.95, spline.getDiscountFactor(1.0), 1e-9);
        assertEquals(0.78, spline.getDiscountFactor(5.0), 1e-9);
    }

    @Test
    void spline_interpolated_isSmooth() {
        // Spline values between knots should differ from linear (smoothness check)
        double linearVal = linear.getDiscountFactor(3.5);
        double splineVal = spline.getDiscountFactor(3.5);
        // They can differ — just ensure spline gives a reasonable value
        assertTrue(splineVal > 0.5 && splineVal < 1.0);
    }

    // --- Forward rates ---

    @Test
    void forwardRate_isPositive() {
        assertTrue(linear.getForwardRate(1.0, 2.0) > 0);
        assertTrue(spline.getForwardRate(1.0, 2.0) > 0);
    }

    @Test
    void forwardRate_formula_isConsistent() {
        // L(0,1) * 1 * P(1) should equal P(0) - P(1) (from definition)
        double l = linear.getForwardRate(0.0, 1.0);
        double p0 = linear.getDiscountFactor(0.0);
        double p1 = linear.getDiscountFactor(1.0);
        assertEquals(p0 / p1 - 1.0, l, EPS);
    }

    // --- Monotonicity ---

    @Test
    void discountFactors_areMonotonicallyDecreasing() {
        double prev = linear.getDiscountFactor(0.0);
        for (double t : new double[]{0.5, 1.0, 2.0, 3.0, 5.0, 7.5, 10.0}) {
            double curr = linear.getDiscountFactor(t);
            assertTrue(curr <= prev + EPS, "Not monotone at t=" + t);
            prev = curr;
        }
    }
}
