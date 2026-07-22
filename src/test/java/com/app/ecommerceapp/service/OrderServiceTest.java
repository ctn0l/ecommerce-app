package com.app.ecommerceapp.service;

import com.app.ecommerceapp.dto.OrderResponse;
import com.app.ecommerceapp.mapper.OrderMapper;
import com.app.ecommerceapp.model.CartItem;
import com.app.ecommerceapp.model.Order;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.model.User;
import com.app.ecommerceapp.model.enums.OrderStatus;
import com.app.ecommerceapp.repository.CartItemRepository;
import com.app.ecommerceapp.repository.OrderRepository;
import com.app.ecommerceapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                cartItemRepository,
                userRepository,
                orderMapper
        );
    }

    @Test
    void createsOrderFromCart() {
        User user = new User();
        user.setId(1L);
        Product keyboard = product(10L, "Keyboard", 5);
        Product mouse = product(11L, "Mouse", 10);
        List<CartItem> cartItems = List.of(
                cartItem(user, keyboard, 2, "39.99"),
                cartItem(user, mouse, 1, "20.00")
        );
        OrderResponse expectedResponse = new OrderResponse(
                50L,
                1L,
                new BigDecimal("99.98"),
                OrderStatus.PENDING,
                List.of(),
                null,
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(1L)).thenReturn(cartItems);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(50L);
            return order;
        });
        when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

        OrderResponse response = orderService.createOrder("1");

        assertThat(response).isSameAs(expectedResponse);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getUser()).isSameAs(user);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("99.98");
        assertThat(savedOrder.getItems()).hasSize(2)
                .allSatisfy(item -> assertThat(item.getOrder()).isSameAs(savedOrder));
        assertThat(keyboard.getStockQuantity()).isEqualTo(3);
        assertThat(mouse.getStockQuantity()).isEqualTo(9);
        verify(cartItemRepository).deleteAll(cartItems);
        verify(orderMapper).toResponse(savedOrder);
    }

    @Test
    void rejectsInvalidUserId() {
        assertThatThrownBy(() -> orderService.createOrder("invalid"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(userRepository, cartItemRepository, orderRepository, orderMapper);
    }

    @Test
    void rejectsMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder("99"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verifyNoInteractions(cartItemRepository, orderRepository, orderMapper);
    }

    @Test
    void rejectsEmptyCart() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder("1"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void rejectsProductWithInsufficientStock() {
        User user = new User();
        Product product = product(10L, "Keyboard", 1);
        CartItem cartItem = cartItem(user, product, 2, "39.99");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findAllByUserId(1L)).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder("1"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        assertThat(product.getStockQuantity()).isEqualTo(1);
        verifyNoInteractions(orderRepository, orderMapper);
    }

    private Product product(Long id, String name, int stockQuantity) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setActive(true);
        product.setStockQuantity(stockQuantity);
        return product;
    }

    private CartItem cartItem(
            User user,
            Product product,
            int quantity,
            String unitPrice
    ) {
        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(new BigDecimal(unitPrice));
        return item;
    }
}
