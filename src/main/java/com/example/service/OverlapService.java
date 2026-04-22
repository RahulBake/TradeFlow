package com.example.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class OverlapService {

    private static final Map<String, Set<String>> BASKETS = buildBaskets();

    private OverlapService() {
    }

    public static OverlapAnalysis calculate(Set<String> portfolio) {
        Set<String> normalizedPortfolio = Set.copyOf(portfolio);
        Map<String, Double> result = new LinkedHashMap<>();

        for (var e : BASKETS.entrySet()) {
            Set<String> common = new HashSet<>(normalizedPortfolio);
            common.retainAll(e.getValue());

            double denominator = normalizedPortfolio.size() + e.getValue().size();
            double val = denominator == 0 ? 0.0 : (2.0 * common.size() / denominator) * 100;
            result.put(e.getKey(), val);
        }

        Map.Entry<String, Double> dominant = null;
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            if (dominant == null || entry.getValue() > dominant.getValue()) {
                dominant = entry;
            }
        }

        return new OverlapAnalysis(result, dominant.getKey(), riskFlag(dominant.getValue()));
    }

    private static String riskFlag(double maxOverlap) {
        if (maxOverlap >= 60.0) {
            return "HIGH";
        }
        if (maxOverlap >= 40.0) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public record OverlapAnalysis(
            Map<String, Double> overlaps,
            String dominantBasket,
            String riskFlag) {
    }

    private static Map<String, Set<String>> buildBaskets() {
        Map<String, Set<String>> baskets = new LinkedHashMap<>();
        baskets.put("TECH_HEAVY", Set.of("AAPL", "MSFT", "GOOGL", "TSLA", "NVDA"));
        baskets.put("FINANCE_HEAVY", Set.of("JPM", "GS", "BAC", "MS", "WFC"));
        baskets.put("BALANCED", Set.of("AAPL", "JPM", "XOM", "JNJ", "TSLA"));
        return Collections.unmodifiableMap(baskets);
    }
}
