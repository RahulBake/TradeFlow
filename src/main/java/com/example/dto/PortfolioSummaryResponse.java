package com.example.dto;

import java.util.Map;

public record PortfolioSummaryResponse(
        String traderId,
        Map<String, Integer> positions,
        Map<String, Integer> sectorBreakdown) {
}
