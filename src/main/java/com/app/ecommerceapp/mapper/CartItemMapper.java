package com.app.ecommerceapp.mapper;

import com.app.ecommerceapp.dto.CartItemResponse;
import com.app.ecommerceapp.model.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemMapper {

    public CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        BigDecimal subtotal = cartItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                subtotal
        );
    }
}
