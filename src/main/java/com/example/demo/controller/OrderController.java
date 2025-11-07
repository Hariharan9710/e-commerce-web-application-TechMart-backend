package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private String extractEmailFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Not authenticated");
        }
        String token = authorization.substring(7);
        byte[] decoded = Base64.getDecoder().decode(token);
        return new String(decoded);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Order order) {

        String email = extractEmailFromToken(authorization);

        // ✅ Pass shippingAddress and paymentMethod from request body
        return ResponseEntity.ok(
                orderService.createOrder(email, order.getShippingAddress(), order.getPaymentMethod())
        );
    }

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(
            @RequestHeader("Authorization") String authorization) {

        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(orderService.getUserOrders(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) { 

        extractEmailFromToken(authorization); // Just validate
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
 // User cancels order
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestParam String reason) {
        
        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(orderService.cancelOrder(id, email, reason));
    }
 // User requests return
    @PutMapping("/{id}/request-return")
    public ResponseEntity<?> requestReturn(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String images) {
        
        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(orderService.requestReturn(id, email, reason, images));
    }
}
