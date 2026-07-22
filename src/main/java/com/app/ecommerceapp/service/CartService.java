package com.app.ecommerceapp.service;

import com.app.ecommerceapp.dto.CartItemRequest;
import com.app.ecommerceapp.dto.CartItemResponse;
import com.app.ecommerceapp.mapper.CartItemMapper;
import com.app.ecommerceapp.model.CartItem;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.model.User;
import com.app.ecommerceapp.repository.CartItemRepository;
import com.app.ecommerceapp.repository.ProductRepository;
import com.app.ecommerceapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartItemMapper cartItemMapper;

    @Transactional
    public void addToCart(String userId, CartItemRequest request) {
        Long parsedUserId = parseUserId(userId);
        User user = userRepository.findById(parsedUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        ensureProductCanBePurchased(product);

        CartItem cartItem = cartItemRepository
                .findByUserAndProduct(user, product)
                .orElseGet(() -> newCartItem(user, product));
        long updatedQuantity = (long) cartItem.getQuantity() + request.quantity();

        if (updatedQuantity > product.getStockQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Requested quantity exceeds available stock"
            );
        }

        cartItem.setQuantity((int) updatedQuantity);
        cartItem.setUnitPrice(product.getPrice());
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public boolean deleteItemFromCart(String userId, Long productId) {
        Long parsedUserId = parseUserId(userId);

        return userRepository.findById(parsedUserId)
                .flatMap(user -> productRepository.findById(productId)
                        .flatMap(product -> cartItemRepository.findByUserAndProduct(user, product)))
                .map(cartItem -> {
                    cartItemRepository.delete(cartItem);
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(String userId) {
        Long parsedUserId = parseUserId(userId);
        return cartItemRepository.findAllByUserId(parsedUserId).stream()
                .map(cartItemMapper::toResponse)
                .toList();
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

    private void ensureProductCanBePurchased(Product product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is not active");
        }
    }

    private CartItem newCartItem(User user, Product product) {
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(0);
        return cartItem;
    }
}
