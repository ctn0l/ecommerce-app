package com.app.ecommerceapp.mapper;

import com.app.ecommerceapp.dto.OrderItemDTO;
import com.app.ecommerceapp.dto.OrderResponse;
import com.app.ecommerceapp.model.Order;
import com.app.ecommerceapp.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getItems().stream()
                        .map(this::toItemDTO)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemDTO toItemDTO(OrderItem item) {
        BigDecimal subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemDTO(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice(),
                subtotal
        );
    }
}
