package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.PortfolioAddRequest;
import com.example.dto.PortfolioSummaryResponse;
import com.example.entities.Order;
import com.example.entities.Portfolio;
import com.example.repository.PortfolioRepository;
import com.example.util.OrderSide;
import com.example.util.OrderStatus;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private TraderService traderService;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void returnsGroupedPortfolioSummary() {
        when(portfolioRepository.findByTraderId("T001")).thenReturn(List.of(
                new Portfolio(1L, "T001", "AAPL", "TECH", 150, 0),
                new Portfolio(2L, "T001", "TSLA", "TECH", 80, 0),
                new Portfolio(3L, "T001", "JPM", "FINANCE", 20, 0)));

        PortfolioSummaryResponse summary = portfolioService.getPortfolio("t001");

        assertEquals(3, summary.positions().size());
        assertEquals(150, summary.positions().get("AAPL"));
        assertEquals(230, summary.sectorBreakdown().get("TECH"));
        assertEquals(20, summary.sectorBreakdown().get("FINANCE"));
    }

    @Test
    void addToPortfolioCreatesOrUpdatesPosition() {
        when(portfolioRepository.findByTraderIdAndStockForUpdate("T001", "NVDA")).thenReturn(Optional.empty());
        when(portfolioRepository.findByTraderId("T001")).thenReturn(List.of(
                new Portfolio(4L, "T001", "NVDA", "TECH", 100, 0)));

        PortfolioSummaryResponse summary = portfolioService.addToPortfolio("t001", new PortfolioAddRequest("nvda", "tech", 100));

        verify(traderService).lockOrCreate("T001");
        verify(portfolioRepository).save(org.mockito.ArgumentMatchers.any(Portfolio.class));
        assertEquals(100, summary.positions().get("NVDA"));
        assertEquals(100, summary.sectorBreakdown().get("TECH"));
    }

    @Test
    void removeDeletesPositionWhenQuantityBecomesZero() {
        Order sellOrder = new Order(10L, "T001", "AAPL", "TECH", 50, OrderSide.SELL, OrderStatus.PENDING, 0);
        Portfolio portfolio = new Portfolio(1L, "T001", "AAPL", "TECH", 50, 0);
        when(portfolioRepository.findByTraderIdAndStockForUpdate("T001", "AAPL")).thenReturn(Optional.of(portfolio));

        portfolioService.remove(sellOrder);

        verify(portfolioRepository).delete(portfolio);
    }
}
