package com.app.ecommerceapp.mapper;

import com.app.ecommerceapp.dto.CartItemResponse;
import com.app.ecommerceapp.model.CartItem;
import com.app.ecommerceapp.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemMapperTest {

    private final CartItemMapper cartItemMapper = new CartItemMapper();

    @Test
    void mapsCartItemToResponse() {
        Product product = new Product();
        product.setId(10L);
        product.setName("Keyboard");

        CartItem cartItem = new CartItem();
        cartItem.setId(20L);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);
        cartItem.setUnitPrice(new BigDecimal("19.99"));

        CartItemResponse response = cartItemMapper.toResponse(cartItem);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.productId()).isEqualTo(10L);
        assertThat(response.productName()).isEqualTo("Keyboard");
        assertThat(response.quantity()).isEqualTo(3);
        assertThat(response.unitPrice()).isEqualByComparingTo("19.99");
        assertThat(response.subtotal()).isEqualByComparingTo("59.97");
    }

    @Test
    void mapsNullCartItemToNull() {
        assertThat(cartItemMapper.toResponse(null)).isNull();
    }
}
