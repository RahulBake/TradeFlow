package com.example.dto;

import com.example.util.OrderSide;

public record OrderRequest(
        String traderId,
        String stock,
        String sector,
        int quantity,
        OrderSide side) {
}
