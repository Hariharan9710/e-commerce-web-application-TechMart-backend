
package com.example.demo.controller;

import com.example.demo.entity.Review;
import com.example.demo.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private String extractEmailFromToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new RuntimeException("Not authenticated");
        }
        String token = authorization.substring(7);
        byte[] decoded = Base64.getDecoder().decode(token);
        return new String(decoded);
    }

    /**
     * ✅ Add a review for a product with proper error handling
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> addReview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> reviewData) {

        try {
            String email = extractEmailFromToken(authorization);
            int rating = Integer.parseInt(reviewData.get("rating").toString());
            String comment = reviewData.get("comment").toString();

            if (rating < 1 || rating > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Rating must be between 1 and 5 stars");
                return ResponseEntity.badRequest().body(error);
            }

            if (comment == null || comment.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please write a review comment");
                return ResponseEntity.badRequest().body(error);
            }

            Review savedReview = reviewService.addReview(email, productId, rating, comment);
            return ResponseEntity.ok(savedReview);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            if (e.getMessage().contains("purchased") || e.getMessage().contains("order")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to submit review. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✅ Get all reviews for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    /**
     * ✅ Get user's own reviews
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<List<Review>> getMyReviews(
            @RequestHeader("Authorization") String authorization) {
        String email = extractEmailFromToken(authorization);
        return ResponseEntity.ok(reviewService.getReviewsByUser(email));
    }

    /**
     * ✅ Delete a review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long reviewId) {
        try {
            String email = extractEmailFromToken(authorization);
            reviewService.deleteReview(email, reviewId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Review deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ✅ NEW: Update a review (added without changing any existing code)
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> reviewData) {

        try {
            String email = extractEmailFromToken(authorization);
            String comment = reviewData.get("comment").toString();
            Integer rating = reviewData.containsKey("rating") ? Integer.parseInt(reviewData.get("rating").toString()) : null;

            if (comment == null || comment.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Review comment cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            Review updated = reviewService.updateReview(email, reviewId, comment, rating);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update review");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
