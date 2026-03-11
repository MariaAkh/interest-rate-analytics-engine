package com.quantlib.montecarlo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonteCarloTest {

    // Black-Scholes analytical call price for comparison
    private double bsCall(double s0, double k, double r, double sigma, double t) {
        double d1 = (Math.log(s0 / k) + (r + 0.5 * sigma * sigma) * t) / (sigma * Math.sqrt(t));
        double d2 = d1 - sigma * Math.sqrt(t);
        return s0 * normalCDF(d1) - k * Math.exp(-r * t) * normalCDF(d2);
    }

    private double normalCDF(double x) {
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }

    private double erf(double x) {
        // Abramowitz and Stegun approximation
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t)
                + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return x >= 0 ? y : -y;
    }

    @Test
    void callOption_convergesToBlackScholes() {
        double s0 = 100, strike = 100, r = 0.05, sigma = 0.2, T = 1.0;
        double df = Math.exp(-r * T);
        double bsPrice = bsCall(s0, strike, r, sigma, T);

        MonteCarloEngine mc = new MonteCarloEngine(500_000, 42L);
        double mcPrice = mc.price(s0, r, sigma, T, df,
            sT -> Math.max(sT - strike, 0.0));

        assertEquals(bsPrice, mcPrice, 0.10,
            String.format("MC=%.4f, BS=%.4f", mcPrice, bsPrice));
    }

    @Test
    void putOption_convergesToBlackScholes() {
        double s0 = 100, strike = 105, r = 0.03, sigma = 0.25, T = 0.5;
        double df = Math.exp(-r * T);
        // Put-call parity: P = C - S0 + K*df
        double bsPut = bsCall(s0, strike, r, sigma, T) - s0 + strike * df;

        MonteCarloEngine mc = new MonteCarloEngine(500_000, 42L);
        double mcPut = mc.price(s0, r, sigma, T, df,
            sT -> Math.max(strike - sT, 0.0));

        assertEquals(bsPut, mcPut, 0.10,
            String.format("MC=%.4f, BS=%.4f", mcPut, bsPut));
    }

    @Test
    void putCallParity_holds() {
        double s0 = 100, strike = 100, r = 0.05, sigma = 0.2, T = 1.0;
        double df = Math.exp(-r * T);

        MonteCarloEngine mc = new MonteCarloEngine(1_000_000, 7L);
        double call = mc.price(s0, r, sigma, T, df, sT -> Math.max(sT - strike, 0.0));
        double put  = mc.price(s0, r, sigma, T, df, sT -> Math.max(strike - sT, 0.0));

        // C - P = S0 - K * e^{-rT}
        double lhs = call - put;
        double rhs = s0 - strike * df;
        assertEquals(rhs, lhs, 0.30, String.format("C-P=%.4f, S0-Ke^-rT=%.4f", lhs, rhs));
    }

    @Test
    void digitalOption_priceIsAProbability() {
        MonteCarloEngine mc = new MonteCarloEngine(200_000, 42L);
        double price = mc.price(100, 0.05, 0.2, 1.0, Math.exp(-0.05),
            sT -> sT > 100 ? 1.0 : 0.0);
        assertTrue(price > 0.3 && price < 0.7,
            "Digital option price should be a probability: " + price);
    }

    @Test
    void standardError_decreasesWithMorePaths() {
        double s0 = 100, r = 0.05, sigma = 0.2, T = 1.0, strike = 100;

        MonteCarloEngine mc1k  = new MonteCarloEngine(1_000,   42L);
        MonteCarloEngine mc10k = new MonteCarloEngine(10_000,  42L);

        double se1k  = mc1k.standardError(s0, r, sigma, T, sT -> Math.max(sT - strike, 0.0));
        double se10k = mc10k.standardError(s0, r, sigma, T, sT -> Math.max(sT - strike, 0.0));

        assertTrue(se10k < se1k, "Standard error should decrease with more paths");
    }
}
