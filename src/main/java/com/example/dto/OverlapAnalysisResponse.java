package com.example.dto;

import java.util.List;

public record OverlapAnalysisResponse(
        List<OverlapItemResponse> overlaps,
        String dominantBasket,
        String riskFlag) {
}
