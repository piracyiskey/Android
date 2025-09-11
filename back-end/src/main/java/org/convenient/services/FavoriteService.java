package org.convenient.services;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.ProductWithFavoriteDTO;
import org.convenient.models.*;
import org.convenient.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void addFavorite(String userId, String productId) {
        if (favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent()) return;

        Favorite favorite = new Favorite();
        favorite.setId(UUID.randomUUID().toString());
        favorite.setUser(userRepository.findById(userId).orElseThrow());
        favorite.setProduct(productRepository.findById(productId).orElseThrow());
        favoriteRepository.save(favorite);
    }

    public void removeFavorite(String userId, String productId) {
        favoriteRepository.findByUserIdAndProductId(userId, productId)
                .ifPresent(favoriteRepository::delete);
    }

    public boolean isFavorite(String userId, String productId) {
        return favoriteRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    public List<ProductWithFavoriteDTO> listFavorites(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Favorite> favorites = favoriteRepository.findByUser(user);

        return favorites.stream()
                .map(fav -> new ProductWithFavoriteDTO(fav.getProduct(), true))
                .collect(Collectors.toList());
    }



}
