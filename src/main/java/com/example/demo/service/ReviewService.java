
package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Review addReview(String email, Long productId, int rating, String comment) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean hasPurchased = orderRepository.findByUserId(user.getId()).stream()
                .anyMatch(order -> order.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)));

        if (!hasPurchased) {
            throw new RuntimeException("You can only review products you have purchased");
        }

        reviewRepository.findByUser_IdAndProduct_Id(user.getId(), productId)
                .ifPresent(existingReview -> {
                    throw new RuntimeException("You have already reviewed this product");
                });

        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        Review review = new Review(rating, comment, user, product);
        Review savedReview = reviewRepository.save(review);

        updateProductRating(productId);
        return savedReview;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProduct_Id(productId);
    }

    public List<Review> getReviewsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reviewRepository.findByUser_Id(user.getId());
    }

    private void updateProductRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProduct_Id(productId);
        if (reviews.isEmpty()) return;

        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setRating(Math.round(avgRating * 10.0) / 10.0);
        productRepository.save(product);
    }

    @Transactional
    public void deleteReview(String email, Long reviewId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        updateProductRating(productId);
    }

    // ✅ NEW METHOD (added safely, no existing code changed)
    @Transactional
    public Review updateReview(String email, Long reviewId, String comment, Integer rating) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("You can only update your own reviews");
        }

        if (comment == null || comment.trim().isEmpty()) {
            throw new RuntimeException("Comment cannot be empty");
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        review.setComment(comment);
        if (rating != null) {
            review.setRating(rating);
        }

        Review updatedReview = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());
        return updatedReview;
    }
}

