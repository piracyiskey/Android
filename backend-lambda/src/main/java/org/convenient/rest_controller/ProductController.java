package org.convenient.rest_controller;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.ProductWithFavoriteDTO;
import org.convenient.models.Product;
import org.convenient.security.JwtUtil;
import org.convenient.services.FavoriteService;
import org.convenient.services.ProductService;
import org.convenient.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ProductController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final ProductService productService;
    private final JwtUtil jwtUtil;


    @GetMapping("/products/by-category-name")
    public ResponseEntity<?> getProductsByCategoryName(
            @RequestParam("name") String categoryName,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token, userService);
        List<ProductWithFavoriteDTO> result = productService.getProductsWithFavoriteStatus(categoryName, userId);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/products/hot")
    public ResponseEntity<?> getHotProducts(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token, userService);
        List<ProductWithFavoriteDTO> result = productService.getHotProductsWithFavoriteStatus(userId);
        return ResponseEntity.ok(result);
    }

    // ProductController.java
    @GetMapping("/products/search")
    public ResponseEntity<?> searchProductsByName(
            @RequestParam("name") String keyword,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String userId = jwtUtil.extractUserId(token, userService);
        List<ProductWithFavoriteDTO> result = productService.searchProductsByName(keyword, userId);

        if (result.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductById(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Product product = productService.getProductEntityById(id); // new method you will add
        boolean isFavorite = false;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                String userId = jwtUtil.extractUserId(token, userService);
                isFavorite = favoriteService.isFavorite(userId, id);
            }
        }

        ProductWithFavoriteDTO dto = new ProductWithFavoriteDTO(product, isFavorite);
        return ResponseEntity.ok(dto);
    }


}
