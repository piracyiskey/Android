package org.convenient.repository;

import org.convenient.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByCategory_Name(String categoryName);
    List<Product> findByHotTrue();
    // Add this method:
    List<Product> findByNameContainingIgnoreCase(String name);
}

