# Interest Rate Analytics Engine

![Java CI](https://github.com/MariaAkh/interest-rate-analytics-engine/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)
![Maven](https://img.shields.io/badge/build-Maven-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

A Java library implementing core quantitative finance models for interest rate analytics, swap valuation, and Monte Carlo option pricing — built as part of a **Computational Finance** course (M.Sc. Mathematics, LMU Munich).

---

## Features

| Module | Description |
|--------|-------------|
| **Discount Curve Interpolation** | Linear and natural cubic spline interpolation of discount factors P(T; 0) |
| **Forward Rate Derivation** | Simply-compounded LIBOR forward rates L(T₁, T₂; 0) from discount curve |
| **Interest Rate Swap Valuation** | Textbook payer swap valuation, par rate calibration, annuity / DV01 |
| **Monte Carlo Pricing Engine** | GBM simulation with arbitrary payoff functions; verified against Black-Scholes |

---

## Mathematical Background

### Discount Curve
Given market discount factors $P(T_i; 0)$ at pillar maturities, the curve interpolates to arbitrary maturities. The **forward rate** between $T_1$ and $T_2$ is derived as:

$$L(T_1, T_2; 0) = \frac{P(T_1; 0)}{P(T_2; 0) - 1} \cdot \frac{1}{T_2 - T_1}$$

Two methods are implemented:
- **Linear interpolation** — fast, simple, may produce jagged forward curves
- **Natural cubic spline** — C² smooth, stable forward rates (preferred in practice)

### Swap Valuation
A payer swap on tenor grid $T_0, T_1, \ldots, T_n$ pays:

$$V = \sum_{i=0}^{n-1} \bigl(L(T_i, T_{i+1}; 0) - K\bigr) \cdot (T_{i+1} - T_i) \cdot P(T_{i+1}; 0)$$

The **par swap rate** $K^*$ is the fixed rate that sets $V = 0$:

$$K^* = \frac{\sum_i L(T_i, T_{i+1}) \cdot \Delta T_i \cdot P(T_{i+1})}{\sum_i \Delta T_i \cdot P(T_{i+1})}$$

### Monte Carlo Engine
Under the risk-neutral measure $\mathbb{Q}$, asset dynamics follow GBM:

$$S(T) = S_0 \exp\!\left(\left(\mu - \tfrac{1}{2}\sigma^2\right)T + \sigma\sqrt{T}\, Z\right), \quad Z \sim \mathcal{N}(0,1)$$

Option price: $V = e^{-rT} \cdot \mathbb{E}^{\mathbb{Q}}[\text{payoff}(S(T))]$

---

## Test Results

| Test | Result |
|------|--------|
| Linear interpolation at knot points | exact match |
| Cubic spline at knot points | exact match |
| Forward rates positive | ✓ |
| Discount curve monotonically decreasing | ✓ |
| Swap at par rate → value = 0 | ✓ |
| MC call price vs Black-Scholes (500k paths) | within €0.10 |
| Put-call parity (1M paths) | within €0.30 |
| Standard error decreases with path count | ✓ |

---

## Project Structure

```
src/
├── main/java/com/quantlib/
│   ├── curves/
│   │   ├── DiscountCurve.java              # Interface
│   │   ├── LinearInterpolatedCurve.java    # Linear interpolation
│   │   └── CubicSplineCurve.java           # Natural cubic spline
│   ├── instruments/
│   │   └── Swap.java                       # Swap valuation & par rate
│   └── montecarlo/
│       └── MonteCarloEngine.java           # GBM Monte Carlo engine
└── test/java/com/quantlib/
    ├── curves/DiscountCurveTest.java        # 7 tests
    ├── instruments/SwapTest.java            # 6 tests
    └── montecarlo/MonteCarloTest.java       # 5 tests
```

---

## Running the Tests

```bash
mvn clean test
```

---

## Technologies

- **Java 17**, OOP with interfaces and default methods
- **Maven** for dependency management and build
- **JUnit 5** for unit testing with numerical precision assertions
- **GitHub Actions** for continuous integration (CI)
```
