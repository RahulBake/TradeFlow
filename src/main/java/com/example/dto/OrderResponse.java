package com.example.dto;

import com.example.entities.Order;
import com.example.util.OrderSide;
import com.example.util.OrderStatus;

public record OrderResponse(
        Long id,
        String traderId,
        String stock,
        String sector,
        int quantity,
        OrderSide side,
        OrderStatus status) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTraderId(),
                order.getStock(),
                order.getSector(),
                order.getQuantity(),
                order.getSide(),
                order.getStatus());
    }
}
