package com.app.ecommerceapp.service;

import com.app.ecommerceapp.dto.OrderResponse;
import com.app.ecommerceapp.mapper.OrderMapper;
import com.app.ecommerceapp.model.CartItem;
import com.app.ecommerceapp.model.Order;
import com.app.ecommerceapp.model.OrderItem;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.model.User;
import com.app.ecommerceapp.repository.CartItemRepository;
import com.app.ecommerceapp.repository.OrderRepository;
import com.app.ecommerceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(String userId) {
        Long parsedUserId = parseUserId(userId);
        User user = userRepository.findById(parsedUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(parsedUserId);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            validateProduct(product, cartItem.getQuantity());

            OrderItem orderItem = createOrderItem(order, cartItem);
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(
                    orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
            );

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.saveAndFlush(order);
        cartItemRepository.deleteAll(cartItems);

        return orderMapper.toResponse(savedOrder);
    }

    private Long parseUserId(String userId) {
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "X-User-ID must be a valid number",
                    exception
            );
        }
    }

    private void validateProduct(Product product, int requestedQuantity) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product '%s' is not active".formatted(product.getName())
            );
        }
        if (requestedQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock for product '%s'".formatted(product.getName())
            );
        }
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(cartItem.getProduct());
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getUnitPrice());
        return orderItem;
    }
}
