package org.convenient.services;

import lombok.RequiredArgsConstructor;
import org.convenient.models.Rating;
import org.convenient.repository.ProductRepository;
import org.convenient.repository.RatingRepository;
import org.convenient.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<Rating> getRatingsByProduct(String productId) {
        return ratingRepository.findByProductId(productId);
    }

    public Double getAverageStars(String productId) {
        return Optional.ofNullable(ratingRepository.getAverageStars(productId)).orElse(0.0);
    }

    public long getReviewCount(String productId) {
        return ratingRepository.countByProductId(productId);
    }

    public Rating addOrUpdateRating(String userId, String productId, double star, String body) {
        Optional<Rating> optionalRating = ratingRepository.findByUserIdAndProductId(userId, productId);

        Rating rating;

        if (optionalRating.isPresent()) {
            // Update existing rating
            rating = optionalRating.get();
            rating.setStar(star);
            rating.setBody(body);
            rating.setUpdateDate(new Date());
        } else {
            // Create new rating
            rating = Rating.builder()
                    .id(UUID.randomUUID().toString())
                    .user(userRepository.findById(userId).orElseThrow())
                    .product(productRepository.findById(productId).orElseThrow())
                    .star(star)
                    .body(body)
                    .createdDate(new Date())
                    .updateDate(new Date())
                    .build();
        }

        return ratingRepository.save(rating);
    }

}
