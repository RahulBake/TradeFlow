package com.example.dto;

public record PortfolioAddRequest(
        String stock,
        String sector,
        int quantity) {
}
