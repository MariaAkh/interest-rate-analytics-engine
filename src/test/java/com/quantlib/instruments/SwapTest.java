package com.quantlib.instruments;

import com.quantlib.curves.DiscountCurve;
import com.quantlib.curves.LinearInterpolatedCurve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SwapTest {

    private static final double EPS = 1e-9;

    DiscountCurve curve;
    double[] tenors = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0};

    @BeforeEach
    void setUp() {
        curve = new LinearInterpolatedCurve(
            new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0},
            new double[]{1.0, 0.951, 0.904, 0.858, 0.814, 0.772}
        );
    }

    @Test
    void atParRate_swapValueIsZero() {
        double parRate = new Swap(tenors, 0.0).getParSwapRate(curve);
        Swap parSwap = new Swap(tenors, parRate);
        assertEquals(0.0, parSwap.getValue(curve), EPS);
    }

    @Test
    void aboveParRate_payerSwapIsNegative() {
        double parRate = new Swap(tenors, 0.0).getParSwapRate(curve);
        assertTrue(new Swap(tenors, parRate + 0.01).getValue(curve) < 0);
    }

    @Test
    void belowParRate_payerSwapIsPositive() {
        double parRate = new Swap(tenors, 0.0).getParSwapRate(curve);
        assertTrue(new Swap(tenors, parRate - 0.01).getValue(curve) > 0);
    }

    @Test
    void parSwapRate_isReasonable() {
        double parRate = new Swap(tenors, 0.0).getParSwapRate(curve);
        // Expect a small positive rate consistent with the discount curve
        assertTrue(parRate > 0.01 && parRate < 0.15,
            "Par rate out of expected range: " + parRate);
    }

    @Test
    void annuity_isPositive() {
        double annuity = new Swap(tenors, 0.05).getAnnuity(curve);
        assertTrue(annuity > 0);
    }

    @Test
    void swapValue_linearInNotional() {
        // V(2 * K) = 2 * V(K) only holds for zero fixed rate, but
        // scaling check: V(parRate + dx) is proportional to dx * annuity
        double parRate = new Swap(tenors, 0.0).getParSwapRate(curve);
        double annuity = new Swap(tenors, parRate).getAnnuity(curve);
        double dx = 0.001;
        double valueChange = new Swap(tenors, parRate - dx).getValue(curve)
                           - new Swap(tenors, parRate).getValue(curve);
        assertEquals(dx * annuity, valueChange, 1e-10);
    }
}
