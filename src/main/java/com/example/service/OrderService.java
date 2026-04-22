package com.example.service;

import org.springframework.stereotype.Service;

import com.example.dto.OrderRequest;
import com.example.entities.Order;
import com.example.exceptions.BadRequestException;
import com.example.exceptions.ConflictException;
import com.example.exceptions.NotFoundException;
import com.example.repository.OrderRepository;
import com.example.util.OrderSide;
import com.example.util.OrderStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final PortfolioService portfolioService;
    private final TraderService traderService;

    @Transactional
    public Order placeOrder(OrderRequest req) {
        validate(req);
        traderService.lockOrCreate(normalizeTraderId(req.traderId()));

        long count = orderRepo.countByTraderIdAndStatus(normalizeTraderId(req.traderId()), OrderStatus.PENDING);
        if (count >= 3) {
            throw new ConflictException("A trader cannot have more than 3 pending orders.");
        }

        if (req.side() == OrderSide.SELL) {
            int holding = portfolioService.getHolding(normalizeTraderId(req.traderId()), normalizeSymbol(req.stock()));
            if (holding < req.quantity()) {
                throw new ConflictException("SELL order rejected because the trader does not hold enough shares.");
            }
        }

        Order order = new Order();
        order.setTraderId(normalizeTraderId(req.traderId()));
        order.setStock(normalizeSymbol(req.stock()));
        order.setSector(normalizeSector(req.sector()));
        order.setQuantity(req.quantity());
        order.setSide(req.side());
        order.setStatus(OrderStatus.PENDING);
        return orderRepo.save(order);
    }

    @Transactional
    public Order fill(Long id) {
        Order o = orderRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found."));

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only PENDING orders can be filled.");
        }

        traderService.lockOrCreate(o.getTraderId());

        if (o.getSide() == OrderSide.BUY) {
            portfolioService.add(o);
        } else {
            portfolioService.remove(o);
        }

        o.setStatus(OrderStatus.FILLED);
        return o;
    }

    @Transactional
    public Order cancel(Long id) {
        Order o = orderRepo.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found."));

        if (o.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only PENDING orders can be cancelled.");
        }

        o.setStatus(OrderStatus.CANCELLED);
        return o;
    }

    private void validate(OrderRequest req) {
        if (req == null) {
            throw new BadRequestException("Order request body is required.");
        }
        if (isBlank(req.traderId())) {
            throw new BadRequestException("traderId is required.");
        }
        if (isBlank(req.stock())) {
            throw new BadRequestException("stock is required.");
        }
        if (isBlank(req.sector())) {
            throw new BadRequestException("sector is required.");
        }
        if (req.side() == null) {
            throw new BadRequestException("side is required.");
        }
        if (req.quantity() <= 0) {
            throw new BadRequestException("quantity must be greater than 0.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
