package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    /**
     * ✅ Create a new order from user's cart
     */
    @Transactional
    public Order createOrder(String email, String shippingAddress, String paymentMethod) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create order with empty cart");
        }

        // ✅ Step 1: Create the Order object but don't save yet
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
//        order.setStatus("PLACED");
        if (paymentMethod.equalsIgnoreCase("Cash on Delivery")) {
            order.setStatus("ORDER_PLACED");
            order.setPaymentStatus("PENDING"); // COD - payment not yet confirmed
        } else {
            order.setStatus("PAYMENT_PENDING");
            order.setPaymentStatus("PENDING"); // Online payment - needs confirmation
        }
        order.setOrderDate(LocalDateTime.now());

        double totalAmount = 0.0;

        // ✅ Step 2: Convert cart items → order items and calculate total
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }

            // Update stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Create new OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order); // important: link it to this order
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice());

            order.getItems().add(orderItem);

            totalAmount += product.getPrice() * cartItem.getQuantity();
        }

        // ✅ Step 3: Now set totalAmount and save order
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // ✅ Step 4: Clear cart
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }


    /** 
     * ✅ Get all orders for a user
     */
    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserId(user.getId());
    }

    /**
     * ✅ Get a specific order by ID
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
 // Admin confirms payment
    @Transactional
    public Order confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setPaymentStatus("CONFIRMED");
        order.setPaymentConfirmedAt(LocalDateTime.now());
        order.setStatus("PAYMENT_CONFIRMED");
        
        return orderRepository.save(order);
    }

    // Admin updates order status
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(newStatus);
        
        // Set timestamps based on status
        switch (newStatus) {
            case "SHIPPED":
                order.setShippedAt(LocalDateTime.now());
                if (trackingNumber != null) {
                    order.setTrackingNumber(trackingNumber);
                }
                break;
            case "OUT_FOR_DELIVERY":
                // No additional timestamp needed
                break;
            case "DELIVERED":
                order.setDeliveredAt(LocalDateTime.now());
                break;
        }
        
        return orderRepository.save(order);
    }
    
    @Transactional
    public Order requestReturn(Long orderId, String email, String reason, String images) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify ownership
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Check if delivered
        if (!"DELIVERED".equals(order.getStatus())) {
            throw new RuntimeException("Only delivered orders can be returned");
        }
        
        // Check 15-day rule
        LocalDateTime deliveredAt = order.getDeliveredAt();
        if (deliveredAt == null) {
            throw new RuntimeException("Delivery date not found");
        }
        
        LocalDateTime fifteenDaysAgo = LocalDateTime.now().minusDays(15);
        if (deliveredAt.isBefore(fifteenDaysAgo)) {
            throw new RuntimeException("Return period (15 days) has expired");
        }
        
        // Check if already requested
        if (order.getReturnStatus() != null) {
            throw new RuntimeException("Return already requested for this order");
        }
        
        order.setReturnStatus("REQUESTED");
        order.setReturnReason(reason);
        order.setReturnRequestedAt(LocalDateTime.now());
        order.setReturnImages(images);
        
        return orderRepository.save(order);
    }
    
    // User cancels order
    @Transactional
    public Order cancelOrder(Long orderId, String email, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify it's user's order
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }
        
        // Only allow cancellation if not shipped
        if (order.getStatus().equals("SHIPPED") || 
            order.getStatus().equals("OUT_FOR_DELIVERY") || 
            order.getStatus().equals("DELIVERED")) {
            throw new RuntimeException("Cannot cancel order that is already shipped");
        }
        
        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        
        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }
        
        return orderRepository.save(order);
    }
}
