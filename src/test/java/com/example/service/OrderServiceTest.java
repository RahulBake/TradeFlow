package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.dto.OrderRequest;
import com.example.entities.Order;
import com.example.exceptions.ConflictException;
import com.example.repository.OrderRepository;
import com.example.util.OrderSide;
import com.example.util.OrderStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private TraderService traderService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void rejectsFourthPendingOrderForTrader() {
        OrderRequest request = new OrderRequest("t001", "AAPL", "TECH", 50, OrderSide.BUY);
        when(orderRepository.countByTraderIdAndStatus("T001", OrderStatus.PENDING)).thenReturn(3L);

        assertThrows(ConflictException.class, () -> orderService.placeOrder(request));

        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(Order.class));
    }

    @Test
    void rejectsSellWhenHoldingsAreInsufficient() {
        OrderRequest request = new OrderRequest("t001", "AAPL", "TECH", 50, OrderSide.SELL);
        when(orderRepository.countByTraderIdAndStatus("T001", OrderStatus.PENDING)).thenReturn(1L);
        when(portfolioService.getHolding("T001", "AAPL")).thenReturn(20);

        assertThrows(ConflictException.class, () -> orderService.placeOrder(request));
    }

    @Test
    void fillsPendingBuyOrderAndUpdatesStatus() {
        Order order = new Order(10L, "T001", "AAPL", "TECH", 10, OrderSide.BUY, OrderStatus.PENDING, 0);
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));

        Order filled = orderService.fill(10L);

        assertEquals(OrderStatus.FILLED, filled.getStatus());
        verify(portfolioService).add(order);
    }

    @Test
    void cancelsPendingOrder() {
        Order order = new Order(12L, "T001", "AAPL", "TECH", 10, OrderSide.BUY, OrderStatus.PENDING, 0);
        when(orderRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(order));

        Order cancelled = orderService.cancel(12L);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
    }
}
