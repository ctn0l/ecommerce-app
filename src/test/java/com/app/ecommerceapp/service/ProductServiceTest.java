package com.app.ecommerceapp.service;

import com.app.ecommerceapp.dto.ProductRequest;
import com.app.ecommerceapp.dto.ProductResponse;
import com.app.ecommerceapp.mapper.ProductMapper;
import com.app.ecommerceapp.model.Product;
import com.app.ecommerceapp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductMapper productMapper;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productMapper = new ProductMapper();
        productService = new ProductService(productRepository, productMapper);
    }

    @Test
    void createsProductFromRequestAndReturnsResponse() {
        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(
                request("Notebook", new BigDecimal("999.99"))
        );

        assertThat(response.name()).isEqualTo("Notebook");
        assertThat(response.price()).isEqualByComparingTo("999.99");
        assertThat(response.active()).isTrue();
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void returnsMappedProducts() {
        Product product = productMapper.toEntity(request("Notebook", new BigDecimal("999.99")));
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.fetchAllProducts();

        assertThat(responses)
                .singleElement()
                .extracting(ProductResponse::name)
                .isEqualTo("Notebook");
    }

    @Test
    void searchesProductsByNormalizedKeyword() {
        Product product = productMapper.toEntity(request("Notebook", new BigDecimal("999.99")));
        when(productRepository.searchProducts("note"))
                .thenReturn(List.of(product));

        List<ProductResponse> responses = productService.searchProducts("  note  ");

        assertThat(responses)
                .singleElement()
                .extracting(ProductResponse::name)
                .isEqualTo("Notebook");
    }

    @Test
    void searchesAllAvailableProductsWhenKeywordIsBlank() {
        Product product = productMapper.toEntity(request("Notebook", new BigDecimal("999.99")));
        when(productRepository.searchProducts("")).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.searchProducts("   ");

        assertThat(responses).hasSize(1);
        verify(productRepository).searchProducts("");
    }

    @Test
    void updatesAllowedFieldsWithoutChangingActiveStatus() {
        Product existingProduct = productMapper.toEntity(
                request("Notebook", new BigDecimal("999.99"))
        );
        existingProduct.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.saveAndFlush(existingProduct)).thenReturn(existingProduct);

        Optional<ProductResponse> response = productService.updateProduct(
                1L,
                request("Notebook Pro", new BigDecimal("1299.99"))
        );

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().name()).isEqualTo("Notebook Pro");
        assertThat(response.orElseThrow().price()).isEqualByComparingTo("1299.99");
        assertThat(response.orElseThrow().active()).isFalse();
    }

    @Test
    void returnsEmptyWhenProductToUpdateDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ProductResponse> response = productService.updateProduct(
                99L,
                request("Notebook Pro", new BigDecimal("1299.99"))
        );

        assertThat(response).isEmpty();
    }

    private ProductRequest request(String name, BigDecimal price) {
        return new ProductRequest(
                name,
                "Notebook per uso professionale",
                price,
                10,
                "Elettronica",
                "https://example.com/notebook.jpg"
        );
    }
}
