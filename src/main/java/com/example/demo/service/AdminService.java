

package com.example.demo.service;

import com.example.demo.dto.StockSummaryDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.CartItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public AdminService(UserRepository userRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        OrderService orderService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderService = orderService;
    }

    // ==================== PRODUCT MANAGEMENT ====================

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setCategory(productDetails.getCategory());
        product.setBrand(productDetails.getBrand());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());

        if (productDetails.getImage() != null && !productDetails.getImage().isEmpty()) {
            product.setImage(productDetails.getImage());
        }

        product.setRating(productDetails.getRating());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartItemRepository.deleteByProduct(product);
        productRepository.deleteById(id);
    }

    // ==================== STOCK MANAGEMENT ====================

    public List<StockSummaryDTO> getStockSummary() {
        List<Product> products = productRepository.findAll();
        Map<String, StockSummaryDTO> summaryMap = new HashMap<>();

        for (Product product : products) {
            String category = product.getCategory();
            summaryMap.putIfAbsent(category, new StockSummaryDTO(category, 0, 0));

            StockSummaryDTO summary = summaryMap.get(category);
            summary.setTotalStock(summary.getTotalStock() + product.getStock());
            summary.setProductCount(summary.getProductCount() + 1);
        }

        return new ArrayList<>(summaryMap.values());
    }

    public List<Product> getStockByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Product updateStock(Long productId, Integer stock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStock(stock);
        return productRepository.save(product);
    }

    // ==================== DASHBOARD ====================

    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalProducts", productRepository.count());
        dashboard.put("totalOrders", orderRepository.count());
        dashboard.put("totalUsers", userRepository.count());

//        double totalRevenue = orderRepository.findAll().stream()
//                .mapToDouble(Order::getTotalAmount)
//                .sum();
        double totalRevenue = orderRepository.findAll().stream()
                .mapToDouble(o -> o.getTotalAmount() - (o.getRefundedAmount() != null ? o.getRefundedAmount() : 0.0))
                .sum();
        dashboard.put("totalRevenue", totalRevenue);

        List<Product> lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getStock() < 5)
                .collect(Collectors.toList());
        dashboard.put("lowStockProducts", lowStockProducts);

        dashboard.put("stockSummary", getStockSummary());

        return dashboard;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ==================== ORDER MANAGEMENT ====================

    public Order confirmPayment(Long orderId) {
        return orderService.confirmPayment(orderId);
    }

    public Order updateOrderStatus(Long orderId, String status, String trackingNumber) {
        return orderService.updateOrderStatus(orderId, status, trackingNumber);
    }

    // ==================== RETURN MANAGEMENT ====================

    /**
     * ✅ FIXED: Show ALL return requests (REQUESTED, APPROVED, RETURN_RECEIVED, etc.)
     * Now admin can see orders at all stages of the return process
     */
    public List<Order> getAllReturnRequests() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getReturnStatus() != null && 
                           !o.getReturnStatus().equals("REJECTED") &&
                           !o.getReturnStatus().equals("REFUND_COMPLETED") &&
                           !o.getReturnStatus().equals("RETURN_REJECTED"))
                .collect(Collectors.toList());
    }

    @Transactional
    public Order approveReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"REQUESTED".equals(order.getReturnStatus())) {
            throw new RuntimeException("Return request is not in REQUESTED state");
        }

        order.setReturnStatus("APPROVED");
        order.setReturnApprovedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order rejectReturn(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"REQUESTED".equals(order.getReturnStatus())) {
            throw new RuntimeException("Return request is not in REQUESTED state");
        }

        order.setReturnStatus("REJECTED");
        order.setReturnRejectedAt(LocalDateTime.now());
        order.setReturnRejectionReason(reason);

        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmReturnReceived(Long orderId, String condition) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"APPROVED".equals(order.getReturnStatus())) {
            throw new RuntimeException("Return must be in APPROVED state");
        }

        if ("DAMAGED".equals(condition)) {
            // ✅ Product is damaged/dirty/changed - reject the return
            order.setReturnStatus("RETURN_REJECTED");
            order.setReturnRejectionReason("Product returned in unacceptable condition (damaged/dirty/modified)");
        } else {
            // ✅ Product is in good condition - accept it
            order.setReturnStatus("RETURN_RECEIVED");
        }

        order.setReturnReceivedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
    @Transactional
    public Order initiateRefund(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"RETURN_RECEIVED".equals(order.getReturnStatus())) {
            throw new RuntimeException("Can only initiate refund for received returns");
        }

        // ✅ Change status to refund initiated
        order.setReturnStatus("REFUND_INITIATED");
        order.setRefundInitiatedAt(LocalDateTime.now());

        // ✅ Store refunded amount before zeroing total
        order.setRefundedAmount(order.getTotalAmount());

        // ✅ Restore stock to inventory
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        // ✅ User refund processed → Set payable to 0
        order.setTotalAmount(0.0);

        return orderRepository.save(order);
    }

    @Transactional
    public Order completeRefund(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"REFUND_INITIATED".equals(order.getReturnStatus())) {
            throw new RuntimeException("Refund must be initiated first");
        }

        order.setReturnStatus("REFUND_COMPLETED");
        order.setRefundCompletedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
