

package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Get reviews by product
    List<Review> findByProduct_Id(Long productId);

    // Get reviews by user
    List<Review> findByUser_Id(Long userId);

    // Check if user already reviewed a product
    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);
}
