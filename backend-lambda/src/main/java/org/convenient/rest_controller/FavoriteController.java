package org.convenient.rest_controller;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.ProductWithFavoriteDTO;
import org.convenient.security.JwtUtil;
import org.convenient.services.FavoriteService;
import org.convenient.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    private String getUserIdFromHeader(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractUserId(token, userService);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFavorite(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam("productId") String productId) {
        favoriteService.addFavorite(getUserIdFromHeader(authHeader), productId);
        return ResponseEntity.ok("added");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFavorite(@RequestHeader("Authorization") String authHeader,
                                                 @RequestParam("productId") String productId) {
        favoriteService.removeFavorite(getUserIdFromHeader(authHeader), productId);
        return ResponseEntity.ok("removed");
    }

    @GetMapping
    public ResponseEntity<List<ProductWithFavoriteDTO>> listFavorites(@RequestHeader("Authorization") String authHeader) {
        List<ProductWithFavoriteDTO> favorites = favoriteService.listFavorites(getUserIdFromHeader(authHeader));
        return ResponseEntity.ok(favorites);
    }

}
