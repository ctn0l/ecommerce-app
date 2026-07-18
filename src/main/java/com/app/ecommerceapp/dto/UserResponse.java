package com.app.ecommerceapp.dto;

import com.app.ecommerceapp.model.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        AddressDTO address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
