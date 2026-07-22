# E-commerce App

A RESTful e-commerce backend built with Spring Boot, Spring Data JPA and Hibernate.

The project currently provides APIs for managing users, products, shopping carts and orders. It is under active development and will progressively evolve toward a containerized microservices architecture.

## Current Features

- User and address management
- Product catalog management
- Product search
- Shopping cart management
- Order creation from cart items
- Product stock validation
- DTO-based API responses
- Bean Validation
- Transactional service layer
- Unit and integration tests

## Tech Stack

- Java 26
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Hibernate
- Jakarta Bean Validation
- H2 Database
- Maven
- JUnit 5
- Mockito
- AssertJ

## Getting Started

### Requirements

- Java 26

### Run the application

  ```bash
  ./mvnw spring-boot:run
  ```

  The application will start at:

  http://localhost:8080

  ### Run the tests

  ./mvnw test


## API Overview

### Users

- POST /api/users — Create a user
- GET /api/users — Retrieve all users

### Products

- POST /api/products — Create a product
- GET /api/products — Retrieve all products

### Cart

- GET /api/cart — Retrieve the user's cart
- POST /api/cart — Add a product to the cart
- DELETE /api/cart/items/{productId} — Remove a product from the cart

### Orders

- POST /api/orders — Create an order from the user's cart

  Cart and order endpoints currently identify the user through the request header:

  X-User-ID: 1

  ## Database

  The project currently uses an in-memory H2 database.

  The H2 console is available at:

  http://localhost:8080/h2-console

  Current JDBC URL:

  jdbc:h2:mem:test

  Data is reset whenever the application restarts.
