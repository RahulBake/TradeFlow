package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

class OverlapServiceTest {

    @Test
    void calculatesDominantBasketAndHighRiskFlag() {
        OverlapService.OverlapAnalysis analysis = OverlapService.calculate(Set.of("AAPL", "TSLA", "NVDA"));

        assertEquals(75.0, analysis.overlaps().get("TECH_HEAVY"));
        assertEquals(0.0, analysis.overlaps().get("FINANCE_HEAVY"));
        assertEquals(50.0, analysis.overlaps().get("BALANCED"));
        assertEquals("TECH_HEAVY", analysis.dominantBasket());
        assertEquals("HIGH", analysis.riskFlag());
    }

    @Test
    void returnsLowRiskForEmptyPortfolio() {
        OverlapService.OverlapAnalysis analysis = OverlapService.calculate(Set.of());

        assertEquals(0.0, analysis.overlaps().get("TECH_HEAVY"));
        assertEquals(0.0, analysis.overlaps().get("FINANCE_HEAVY"));
        assertEquals(0.0, analysis.overlaps().get("BALANCED"));
        assertEquals("TECH_HEAVY", analysis.dominantBasket());
        assertEquals("LOW", analysis.riskFlag());
    }
}
