package com.quantlib.montecarlo;

import java.util.Random;
import java.util.function.Function;

/**
 * Monte Carlo pricing engine using Geometric Brownian Motion (GBM).
 *
 * Simulates: S(T) = S0 * exp((mu - 0.5*sigma^2)*T + sigma*sqrt(T)*Z)
 * where Z ~ N(0,1).
 *
 * Supports arbitrary payoff functions via Java functional interface.
 */
public class MonteCarloEngine {

    private final int numPaths;
    private final long seed;

    public MonteCarloEngine(int numPaths, long seed) {
        if (numPaths < 1) throw new IllegalArgumentException("numPaths must be positive.");
        this.numPaths = numPaths;
        this.seed = seed;
    }

    /**
     * Estimates the discounted expected payoff E[payoff(S(T))] * discountFactor.
     *
     * @param s0             initial asset price
     * @param mu             drift (risk-free rate under Q measure)
     * @param sigma          volatility
     * @param maturity       time to maturity T
     * @param discountFactor e^{-rT}
     * @param payoff         payoff function of terminal price S(T)
     * @return               estimated option price
     */
    public double price(double s0, double mu, double sigma, double maturity,
                        double discountFactor, Function<Double, Double> payoff) {
        Random rng = new Random(seed);
        double sum = 0.0;
        double drift = (mu - 0.5 * sigma * sigma) * maturity;
        double diffusion = sigma * Math.sqrt(maturity);

        for (int i = 0; i < numPaths; i++) {
            double z = rng.nextGaussian();
            double sT = s0 * Math.exp(drift + diffusion * z);
            sum += payoff.apply(sT);
        }
        return discountFactor * sum / numPaths;
    }

    /**
     * Returns the Monte Carlo standard error (uncertainty of the price estimate).
     * Useful for convergence analysis and reporting confidence intervals.
     */
    public double standardError(double s0, double mu, double sigma,
                                double maturity, Function<Double, Double> payoff) {
        Random rng = new Random(seed);
        double sum = 0.0, sumSq = 0.0;
        double drift = (mu - 0.5 * sigma * sigma) * maturity;
        double diffusion = sigma * Math.sqrt(maturity);

        for (int i = 0; i < numPaths; i++) {
            double z = rng.nextGaussian();
            double sT = s0 * Math.exp(drift + diffusion * z);
            double p = payoff.apply(sT);
            sum += p;
            sumSq += p * p;
        }
        double mean = sum / numPaths;
        double variance = (sumSq / numPaths) - mean * mean;
        return Math.sqrt(variance / numPaths);
    }

    public int getNumPaths() { return numPaths; }
}
