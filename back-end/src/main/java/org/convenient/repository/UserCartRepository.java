package org.convenient.repository;

import org.convenient.models.UserCart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCartRepository extends JpaRepository<UserCart, String> {
    Optional<UserCart> findByUserId(String userId);
}