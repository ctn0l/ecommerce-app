package com.app.ecommerceapp.mapper;

import com.app.ecommerceapp.dto.OrderResponse;
import com.app.ecommerceapp.model.Order;
import com.app.ecommerceapp.model.OrderItem;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.model.User;
import com.app.ecommerceapp.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void mapsOrderToResponse() {
        User user = new User();
        user.setId(5L);

        Product product = new Product();
        product.setId(10L);
        product.setName("Keyboard");

        Order order = new Order();
        order.setId(20L);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("79.98"));
        order.setCreatedAt(LocalDateTime.of(2026, 7, 22, 10, 0));
        order.setUpdatedAt(LocalDateTime.of(2026, 7, 22, 10, 1));

        OrderItem item = new OrderItem();
        item.setId(30L);
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("39.99"));
        order.getItems().add(item);

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.totalAmount()).isEqualByComparingTo("79.98");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.items()).singleElement().satisfies(itemResponse -> {
            assertThat(itemResponse.id()).isEqualTo(30L);
            assertThat(itemResponse.productId()).isEqualTo(10L);
            assertThat(itemResponse.productName()).isEqualTo("Keyboard");
            assertThat(itemResponse.quantity()).isEqualTo(2);
            assertThat(itemResponse.unitPrice()).isEqualByComparingTo("39.99");
            assertThat(itemResponse.subtotal()).isEqualByComparingTo("79.98");
        });
    }

    @Test
    void mapsNullOrderToNull() {
        assertThat(orderMapper.toResponse(null)).isNull();
    }
}
