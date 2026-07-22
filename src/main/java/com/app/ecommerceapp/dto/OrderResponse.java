package com.app.ecommerceapp.dto;

import com.app.ecommerceapp.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal totalAmount,
        OrderStatus status,
        List<OrderItemDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
