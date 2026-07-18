package com.app.ecommerceapp.repository;

import com.app.ecommerceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
