package com.example.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.dto.OverlapAnalysisResponse;
import com.example.dto.OverlapItemResponse;
import com.example.dto.PortfolioAddRequest;
import com.example.dto.PortfolioSummaryResponse;
import com.example.entities.Order;
import com.example.entities.Portfolio;
import com.example.exceptions.BadRequestException;
import com.example.exceptions.ConflictException;
import com.example.repository.PortfolioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository repo;
    private final TraderService traderService;

    public int getHolding(String traderId, String stock) {
        return repo.findByTraderIdAndStock(traderId, stock)
                .map(Portfolio::getQuantity)
                .orElse(0);
    }

    @Transactional
    public void add(Order o) {
        traderService.ensureExists(o.getTraderId());
        Portfolio p = repo.findByTraderIdAndStockForUpdate(o.getTraderId(), o.getStock())
                .orElse(new Portfolio(null, o.getTraderId(), o.getStock(), o.getSector(), 0, 0));

        p.setQuantity(p.getQuantity() + o.getQuantity());
        p.setSector(o.getSector());
        repo.save(p);
    }

    @Transactional
    public void remove(Order o) {
        Portfolio p = repo.findByTraderIdAndStockForUpdate(o.getTraderId(), o.getStock())
                .orElseThrow(() -> new ConflictException("Cannot sell a stock that is not present in the portfolio."));

        int updatedQuantity = p.getQuantity() - o.getQuantity();
        if (updatedQuantity < 0) {
            throw new ConflictException("SELL fill rejected because holdings would become negative.");
        }
        if (updatedQuantity == 0) {
            repo.delete(p);
            return;
        }

        p.setQuantity(updatedQuantity);
        repo.save(p);
    }

    @Transactional
    public PortfolioSummaryResponse addToPortfolio(String traderId, PortfolioAddRequest request) {
        validateAddRequest(traderId, request);
        String normalizedTraderId = normalizeTraderId(traderId);
        String stock = normalizeSymbol(request.stock());
        String sector = normalizeSector(request.sector());

        traderService.lockOrCreate(normalizedTraderId);
        Portfolio portfolio = repo.findByTraderIdAndStockForUpdate(normalizedTraderId, stock)
                .orElse(new Portfolio(null, normalizedTraderId, stock, sector, 0, 0));

        portfolio.setSector(sector);
        portfolio.setQuantity(portfolio.getQuantity() + request.quantity());
        repo.save(portfolio);
        return getPortfolio(normalizedTraderId);
    }

    public PortfolioSummaryResponse getPortfolio(String traderId) {
        String normalizedTraderId = normalizeTraderId(traderId);
        List<Portfolio> holdings = repo.findByTraderId(normalizedTraderId);

        Map<String, Integer> positions = new LinkedHashMap<>();
        Map<String, Integer> sectorBreakdown = new LinkedHashMap<>();

        for (Portfolio holding : holdings) {
            if (holding.getQuantity() <= 0) {
                continue;
            }
            positions.put(holding.getStock(), holding.getQuantity());
            sectorBreakdown.merge(holding.getSector(), holding.getQuantity(), Integer::sum);
        }

        return new PortfolioSummaryResponse(normalizedTraderId, positions, sectorBreakdown);
    }

    public OverlapAnalysisResponse analyzeOverlap(String traderId) {
        PortfolioSummaryResponse summary = getPortfolio(traderId);
        Set<String> portfolioStocks = summary.positions().keySet();
        OverlapService.OverlapAnalysis analysis = OverlapService.calculate(portfolioStocks);

        List<OverlapItemResponse> overlaps = new ArrayList<>();
        DecimalFormat format = new DecimalFormat("0.00");
        for (Map.Entry<String, Double> entry : analysis.overlaps().entrySet()) {
            overlaps.add(new OverlapItemResponse(entry.getKey(), format.format(entry.getValue()) + "%"));
        }

        return new OverlapAnalysisResponse(overlaps, analysis.dominantBasket(), analysis.riskFlag());
    }

    public Set<String> getHeldStocks(String traderId) {
        return repo.findByTraderId(normalizeTraderId(traderId)).stream()
                .filter(portfolio -> portfolio.getQuantity() > 0)
                .map(Portfolio::getStock)
                .collect(Collectors.toSet());
    }

    private void validateAddRequest(String traderId, PortfolioAddRequest request) {
        if (traderId == null || traderId.isBlank()) {
            throw new BadRequestException("traderId is required.");
        }
        if (request == null) {
            throw new BadRequestException("Portfolio request body is required.");
        }
        if (request.stock() == null || request.stock().isBlank()) {
            throw new BadRequestException("stock is required.");
        }
        if (request.sector() == null || request.sector().isBlank()) {
            throw new BadRequestException("sector is required.");
        }
        if (request.quantity() <= 0) {
            throw new BadRequestException("quantity must be greater than 0.");
        }
    }

    private String normalizeTraderId(String traderId) {
        return traderId.trim().toUpperCase();
    }

    private String normalizeSymbol(String stock) {
        return stock.trim().toUpperCase();
    }

    private String normalizeSector(String sector) {
        return sector.trim().toUpperCase();
    }
}
