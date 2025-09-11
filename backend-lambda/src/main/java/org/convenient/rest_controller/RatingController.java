package org.convenient.rest_controller;

import lombok.RequiredArgsConstructor;
import org.convenient.models.Rating;
import org.convenient.security.JwtUtil;
import org.convenient.services.RatingService;
import org.convenient.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/{productId}")
    public ResponseEntity<?> getRatings(@PathVariable String productId) {
        List<Rating> ratings = ratingService.getRatingsByProduct(productId);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<Map<String, Object>> result = ratings.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("profileURL", r.getUser().getProfile_pic());
            map.put("userName", r.getUser().getFull_name());
            map.put("star", r.getStar());
            map.put("body", r.getBody());
            map.put("createdDate", formatter.format(r.getCreatedDate())); // <--- Format as string
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{productId}/summary")
    public ResponseEntity<?> getRatingSummary(@PathVariable String productId) {
        double avg = ratingService.getAverageStars(productId);
        long count = ratingService.getReviewCount(productId);

        Map<String, Object> result = new HashMap<>();
        result.put("average", avg);
        result.put("count", count);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{productId}/post")
    public ResponseEntity<?> postRating(
            @PathVariable String productId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        String userId = jwtUtil.extractUserId(token, userService);

        Number starObj = (Number) payload.get("star");
        double star = starObj.doubleValue();

        String body = (String) payload.get("body");

        Rating rating = ratingService.addOrUpdateRating(userId, productId, star, body);
        return ResponseEntity.ok("Rating saved successfully");
    }


}

