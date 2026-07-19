package com.app.ecommerceapp.service;

import com.app.ecommerceapp.dto.ProductRequest;
import com.app.ecommerceapp.dto.ProductResponse;
import com.app.ecommerceapp.mapper.ProductMapper;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> fetchAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public Optional<ProductResponse> fetchProduct(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse);
    }

    public List<ProductResponse> searchProducts(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.strip();
        return productRepository.searchProducts(normalizedKeyword)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        Product savedProduct = productRepository.saveAndFlush(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public Optional<ProductResponse> updateProduct(Long id, ProductRequest productRequest) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    productMapper.updateEntity(productRequest, existingProduct);
                    Product savedProduct = productRepository.saveAndFlush(existingProduct);
                    return productMapper.toResponse(savedProduct);
                });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return true;
                })
                .orElse(false);
    }
}
