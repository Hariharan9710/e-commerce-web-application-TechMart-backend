
package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Double totalAmount;

    private String shippingAddress;
    private String paymentMethod;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(nullable = false)
    private String paymentStatus = "PENDING";  // PENDING, CONFIRMED, FAILED

    private LocalDateTime paymentConfirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    private String trackingNumber;
    private String cancelReason;

    public Order() {}

    public Order(Long id, User user, List<OrderItem> items, Double totalAmount,
                 String shippingAddress, String paymentMethod, String status,
                 LocalDateTime orderDate, String paymentStatus, LocalDateTime paymentConfirmedAt,
                 LocalDateTime shippedAt, LocalDateTime deliveredAt, LocalDateTime cancelledAt,
                 String trackingNumber, String cancelReason) {

        this.id = id;
        this.user = user;
        this.items = items;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.orderDate = orderDate;

        this.paymentStatus = paymentStatus;
        this.paymentConfirmedAt = paymentConfirmedAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
        this.cancelledAt = cancelledAt;
        this.trackingNumber = trackingNumber;
        this.cancelReason = cancelReason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentConfirmedAt() { return paymentConfirmedAt; }
    public void setPaymentConfirmedAt(LocalDateTime paymentConfirmedAt) {
        this.paymentConfirmedAt = paymentConfirmedAt;
    }

    public LocalDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    // ✅ RETURN FIELDS

    private String returnStatus; 
    private String returnReason;
    private LocalDateTime returnRequestedAt;
    private LocalDateTime returnApprovedAt;
    private LocalDateTime returnRejectedAt;
    private String returnRejectionReason;
    private LocalDateTime returnReceivedAt;
    private LocalDateTime refundInitiatedAt;
    private LocalDateTime refundCompletedAt;
    private String returnImages;

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }

    public LocalDateTime getReturnRequestedAt() { return returnRequestedAt; }
    public void setReturnRequestedAt(LocalDateTime returnRequestedAt) {
        this.returnRequestedAt = returnRequestedAt;
    }

    public LocalDateTime getReturnApprovedAt() { return returnApprovedAt; }
    public void setReturnApprovedAt(LocalDateTime returnApprovedAt) {
        this.returnApprovedAt = returnApprovedAt;
    }

    public LocalDateTime getReturnRejectedAt() { return returnRejectedAt; }
    public void setReturnRejectedAt(LocalDateTime returnRejectedAt) {
        this.returnRejectedAt = returnRejectedAt;
    }

    public String getReturnRejectionReason() { return returnRejectionReason; }
    public void setReturnRejectionReason(String returnRejectionReason) {
        this.returnRejectionReason = returnRejectionReason;
    }

    public LocalDateTime getReturnReceivedAt() { return returnReceivedAt; }
    public void setReturnReceivedAt(LocalDateTime returnReceivedAt) {
        this.returnReceivedAt = returnReceivedAt;
    }

    public LocalDateTime getRefundInitiatedAt() { return refundInitiatedAt; }
    public void setRefundInitiatedAt(LocalDateTime refundInitiatedAt) {
        this.refundInitiatedAt = refundInitiatedAt;
    }

    public LocalDateTime getRefundCompletedAt() { return refundCompletedAt; }
    public void setRefundCompletedAt(LocalDateTime refundCompletedAt) {
        this.refundCompletedAt = refundCompletedAt;
    }

    public String getReturnImages() { return returnImages; }
    public void setReturnImages(String returnImages) { this.returnImages = returnImages; }

    // ✅ ✅ ✅ NEW FIELD - REFUND AMOUNT
    private Double refundedAmount = 0.0;

    public Double getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(Double refundedAmount) {
        this.refundedAmount = refundedAmount;
    }
}

