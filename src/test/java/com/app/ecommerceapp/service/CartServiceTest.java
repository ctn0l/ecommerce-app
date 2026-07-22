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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartItemRepository,
                userRepository,
                productRepository,
                cartItemMapper
        );
    }

    @Test
    void addsNewProductToCart() {
        User user = new User();
        Product product = availableProduct(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        cartService.addToCart("1", new CartItemRequest(2L, 3));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem savedItem = captor.getValue();
        assertThat(savedItem.getUser()).isSameAs(user);
        assertThat(savedItem.getProduct()).isSameAs(product);
        assertThat(savedItem.getQuantity()).isEqualTo(3);
        assertThat(savedItem.getUnitPrice()).isEqualByComparingTo("19.99");
    }

    @Test
    void incrementsQuantityOfExistingCartItem() {
        User user = new User();
        Product product = availableProduct(10);
        CartItem existingItem = new CartItem();
        existingItem.setUser(user);
        existingItem.setProduct(product);
        existingItem.setQuantity(2);
        existingItem.setUnitPrice(product.getPrice());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.of(existingItem));

        cartService.addToCart("1", new CartItemRequest(2L, 3));

        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void rejectsQuantityGreaterThanAvailableStock() {
        User user = new User();
        Product product = availableProduct(4);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart("1", new CartItemRequest(2L, 5)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsInvalidUserId() {
        assertThatThrownBy(() -> cartService.addToCart("not-a-number", new CartItemRequest(2L, 1)))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deletesItemBelongingToUserCart() {
        User user = new User();
        Product product = availableProduct(10);
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product))
                .thenReturn(Optional.of(cartItem));

        boolean deleted = cartService.deleteItemFromCart("1", 2L);

        assertThat(deleted).isTrue();
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void returnsFalseWhenCartItemDoesNotExist() {
        User user = new User();
        Product product = availableProduct(10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

        boolean deleted = cartService.deleteItemFromCart("1", 2L);

        assertThat(deleted).isFalse();
    }

    @Test
    void stopsDeleteWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        boolean deleted = cartService.deleteItemFromCart("99", 2L);

        assertThat(deleted).isFalse();
        verifyNoInteractions(productRepository, cartItemRepository);
    }

    @Test
    void returnsItemsBelongingToUserCart() {
        CartItem firstItem = new CartItem();
        CartItem secondItem = new CartItem();
        CartItemResponse firstResponse = response(1L);
        CartItemResponse secondResponse = response(2L);
        when(cartItemRepository.findAllByUserId(1L))
                .thenReturn(List.of(firstItem, secondItem));
        when(cartItemMapper.toResponse(firstItem)).thenReturn(firstResponse);
        when(cartItemMapper.toResponse(secondItem)).thenReturn(secondResponse);

        List<CartItemResponse> cart = cartService.getCart("1");

        assertThat(cart).containsExactly(firstResponse, secondResponse);
    }

    @Test
    void returnsEmptyListWhenCartHasNoItems() {
        when(cartItemRepository.findAllByUserId(1L)).thenReturn(List.of());

        List<CartItemResponse> cart = cartService.getCart("1");

        assertThat(cart).isEmpty();
    }

    @Test
    void rejectsInvalidUserIdWhenGettingCart() {
        assertThatThrownBy(() -> cartService.getCart("not-a-number"))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verifyNoInteractions(cartItemRepository);
    }

    private Product availableProduct(int stockQuantity) {
        Product product = new Product();
        product.setActive(true);
        product.setStockQuantity(stockQuantity);
        product.setPrice(new BigDecimal("19.99"));
        return product;
    }

    private CartItemResponse response(Long id) {
        return new CartItemResponse(
                id,
                10L,
                "Product",
                1,
                new BigDecimal("19.99"),
                new BigDecimal("19.99")
        );
    }
}
