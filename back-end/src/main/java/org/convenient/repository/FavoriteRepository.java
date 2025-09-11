package org.convenient.repository;

import org.convenient.models.Favorite;
import org.convenient.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FavoriteRepository extends JpaRepository<Favorite, String> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserIdAndProductId(String userId, String productId);

    @Query("SELECT f.product.id FROM Favorite f WHERE f.user.id = :userId")
    Set<String> findFavoriteProductIdsByUserId(@Param("userId") String userId);

}
