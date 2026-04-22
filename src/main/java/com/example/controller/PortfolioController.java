package com.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.OverlapAnalysisResponse;
import com.example.dto.PortfolioAddRequest;
import com.example.dto.PortfolioSummaryResponse;
import com.example.service.PortfolioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/traders/{traderId}/portfolio")
    public PortfolioSummaryResponse getPortfolio(@PathVariable String traderId) {
        return portfolioService.getPortfolio(traderId);
    }

    @GetMapping("/traders/{traderId}/portfolio/overlap")
    public OverlapAnalysisResponse analyzeOverlap(@PathVariable String traderId) {
        return portfolioService.analyzeOverlap(traderId);
    }

    @PostMapping("/traders/{traderId}/portfolio")
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioSummaryResponse addToPortfolio(@PathVariable String traderId, @RequestBody PortfolioAddRequest request) {
        return portfolioService.addToPortfolio(traderId, request);
    }
}
