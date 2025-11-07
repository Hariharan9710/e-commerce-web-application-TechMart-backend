package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.util.ImageUtil; // ✅ Add this import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ImageUtil imageUtil; // ✅ Inject ImageUtil

    // ✅ Updated constructor
    public ProductController(ProductService productService, ImageUtil imageUtil) {
        this.productService = productService;
        this.imageUtil = imageUtil;
    }

    // ✅ Utility method to ensure full URLs for all products
    private List<Product> enrichImageUrls(List<Product> products) {
        return products.stream()
                .peek(p -> p.setImage(imageUtil.getFullImageUrl(p.getImage())))
                .collect(Collectors.toList());
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeatured() {
        return ResponseEntity.ok(enrichImageUrls(productService.getFeaturedProducts()));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(enrichImageUrls(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        product.setImage(imageUtil.getFullImageUrl(product.getImage()));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(enrichImageUrls(productService.getProductsByCategory(category)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        return ResponseEntity.ok(enrichImageUrls(productService.searchProducts(q)));
    }
}
