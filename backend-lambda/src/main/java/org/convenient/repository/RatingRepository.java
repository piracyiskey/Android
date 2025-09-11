package org.convenient.repository;

import org.convenient.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {
    List<Rating> findByProductId(String productId);
    Optional<Rating> findByUserIdAndProductId(String userId, String productId);
    long countByProductId(String productId);
    @Query("SELECT AVG(r.star) FROM Rating r WHERE r.product.id = :productId")
    Double getAverageStars(@Param("productId") String productId);
}
