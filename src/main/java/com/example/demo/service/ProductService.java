
package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.util.ImageUtil;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ImageUtil imageUtil;

    public ProductService(ProductRepository productRepository, ImageUtil imageUtil) {
        this.productRepository = productRepository;
        this.imageUtil = imageUtil;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCase(query, query);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findTop12ByOrderByIdDesc();
    }
}

