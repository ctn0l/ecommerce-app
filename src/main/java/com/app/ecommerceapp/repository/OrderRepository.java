package com.app.ecommerceapp.repository;

import com.app.ecommerceapp.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
