package com.app.ecommerceapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemRequest(
        @NotNull
        @Positive
        Long productId,

        @NotNull
        @Positive
        Integer quantity
) {
}
