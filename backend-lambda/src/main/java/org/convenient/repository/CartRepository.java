package org.convenient.repository;

import org.convenient.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {
    List<Cart> findByUserId(String userId);
    Optional<Cart> findByUserIdAndProductId(String userId, String productId);
}
