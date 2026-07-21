package com.app.ecommerceapp.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemRequestValidationTest {

    private static AutoCloseable validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validatorFactory = factory;
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void acceptsValidRequest() {
        CartItemRequest request = new CartItemRequest(1L, 2);

        Set<ConstraintViolation<CartItemRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsMissingRequiredFields() {
        CartItemRequest request = new CartItemRequest(null, null);

        Set<ConstraintViolation<CartItemRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("productId", "quantity");
    }

    @Test
    void rejectsNonPositiveProductId() {
        CartItemRequest request = new CartItemRequest(0L, 1);

        Set<ConstraintViolation<CartItemRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("productId");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        CartItemRequest request = new CartItemRequest(1L, -1);

        Set<ConstraintViolation<CartItemRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("quantity");
    }
}
