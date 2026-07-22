package com.app.ecommerceapp.repository;

import com.app.ecommerceapp.model.CartItem;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    @EntityGraph(attributePaths = "product")
    List<CartItem> findAllByUserId(Long userId);
}
