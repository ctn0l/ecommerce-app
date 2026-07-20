package com.app.ecommerceapp.repository;

import com.app.ecommerceapp.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
