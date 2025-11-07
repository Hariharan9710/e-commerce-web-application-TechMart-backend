package com.example.demo.controller;

import com.example.demo.dto.StockSummaryDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ==================== PRODUCT MANAGEMENT ====================

    @PostMapping(value = "/products", consumes = "multipart/form-data")
    public ResponseEntity<Product> addProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("brand") String brand,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam("rating") Double rating,
            @RequestParam("image") MultipartFile imageFile
    ) {
        try {
            // ✅ Save file
            String uploadDir = "src/main/resources/static/images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = imageFile.getOriginalFilename();
            Path path = Paths.get(uploadDir + filename);
            Files.write(path, imageFile.getBytes());

            // ✅ Create product
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setBrand(brand);
            product.setPrice(price);
            product.setStock(stock);
            product.setRating(rating);
            product.setImage(filename);
            return ResponseEntity.ok(adminService.addProduct(product)); // ✅ removed null
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    @PutMapping(value = "/products/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("brand") String brand,
            @RequestParam("price") Double price,
            @RequestParam("stock") Integer stock,
            @RequestParam("rating") Double rating,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setBrand(brand);
            product.setPrice(price);
            product.setStock(stock);
            product.setRating(rating);

            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadDir = "public/images/"; // Frontend public folder
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String filename = imageFile.getOriginalFilename();
                Path path = Paths.get(uploadDir + filename);
                Files.write(path, imageFile.getBytes());

                product.setImage(filename);
            }

            return ResponseEntity.ok(adminService.updateProduct(id, product)); // ✅ removed null
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file", e);
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id); // ✅ removed null
        return ResponseEntity.ok().build();
    }

    // ==================== STOCK MANAGEMENT ====================

    @GetMapping("/stock/summary")
    public ResponseEntity<List<StockSummaryDTO>> getStockSummary() {
        return ResponseEntity.ok(adminService.getStockSummary()); // ✅ removed null
    }

    @GetMapping("/stock/category/{category}")
    public ResponseEntity<List<Product>> getStockByCategory(@PathVariable String category) {
        return ResponseEntity.ok(adminService.getStockByCategory(category)); // ✅ removed null
    }

    @PutMapping("/stock/{productId}")
    public ResponseEntity<Product> updateStock(@PathVariable Long productId,
                                               @RequestParam Integer stock) {
        return ResponseEntity.ok(adminService.updateStock(productId, stock)); // ✅ removed null
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardData()); // ✅ removed null
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders()); // ✅ removed null
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers()); // ✅ removed null
    }

    // Admin confirms payment
    @PutMapping("/orders/{id}/confirm-payment")
    public ResponseEntity<Order> confirmPayment(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.confirmPayment(id));
    }

    // Admin updates order status
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String trackingNumber) {
        return ResponseEntity.ok(adminService.updateOrderStatus(id, status, trackingNumber));
    }
 // Get all return requests
    @GetMapping("/returns")
    public ResponseEntity<List<Order>> getAllReturnRequests() {
        return ResponseEntity.ok(adminService.getAllReturnRequests());
    }

    // Approve return request
    @PutMapping("/returns/{orderId}/approve")
    public ResponseEntity<Order> approveReturn(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminService.approveReturn(orderId));
    }

    // Reject return request
    @PutMapping("/returns/{orderId}/reject")
    public ResponseEntity<Order> rejectReturn(
            @PathVariable Long orderId,
            @RequestParam String reason) {
        return ResponseEntity.ok(adminService.rejectReturn(orderId, reason));
    }

    // Confirm return received
    @PutMapping("/returns/{orderId}/received")
    public ResponseEntity<Order> confirmReturnReceived(
            @PathVariable Long orderId,
            @RequestParam String condition) { // "GOOD" or "DAMAGED"
        return ResponseEntity.ok(adminService.confirmReturnReceived(orderId, condition));
    }

    // Initiate refund
    @PutMapping("/returns/{orderId}/refund")
    public ResponseEntity<Order> initiateRefund(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminService.initiateRefund(orderId));
    }

    // Complete refund
    @PutMapping("/returns/{orderId}/refund-complete")
    public ResponseEntity<Order> completeRefund(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminService.completeRefund(orderId));
    }
}
