package org.convenient.services;

import org.convenient.dto.ProductDTO;
import org.convenient.dto.ProductWithFavoriteDTO;
import org.convenient.models.Product;
import org.convenient.repository.FavoriteRepository;
import org.convenient.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    public List<ProductWithFavoriteDTO> getProductsWithFavoriteStatus(String categoryName, String userId) {
        List<Product> products = productRepository.findByCategory_Name(categoryName);
        Set<String> favoriteIds = favoriteRepository.findFavoriteProductIdsByUserId(userId);

        return products.stream()
                .map(p -> new ProductWithFavoriteDTO(p, favoriteIds.contains(p.getId())))
                .collect(Collectors.toList());
    }


    public List<ProductWithFavoriteDTO> getHotProductsWithFavoriteStatus(String userId) {
        List<Product> hotProducts = productRepository.findByHotTrue();
        Set<String> favoriteIds = favoriteRepository.findFavoriteProductIdsByUserId(userId);

        return hotProducts.stream()
                .map(product -> new ProductWithFavoriteDTO(product, favoriteIds.contains(product.getId())))
                .collect(Collectors.toList());
    }

    // ProductService.java
    public List<ProductWithFavoriteDTO> searchProductsByName(String name, String userId) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        Set<String> favoriteIds = favoriteRepository.findFavoriteProductIdsByUserId(userId);

        return products.stream()
                .map(p -> new ProductWithFavoriteDTO(p, favoriteIds.contains(p.getId())))
                .collect(Collectors.toList());
    }


    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return new ProductDTO(product);
    }

    public Product getProductEntityById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }




}
