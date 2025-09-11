package org.convenient.rest_controller;

import lombok.RequiredArgsConstructor;
import org.convenient.security.JwtUtil;
import org.convenient.services.CartService;
import org.convenient.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping("/count")
    public ResponseEntity<?> getCartProductCount(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        int count = cartService.getDistinctProductCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody Map<String, Object> payload) {
        String userId = getUserIdFromHeader(authHeader);
        String productId = (String) payload.get("productId");
        int quantity = (int) payload.get("quantity");

        cartService.addOrUpdateItem(userId, productId, quantity);
        return ResponseEntity.ok("Item added or updated in cart");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, Object> payload) {
        String userId = getUserIdFromHeader(authHeader);
        String productId = (String) payload.get("productId");
        int quantity = (int) payload.get("quantity");

        cartService.updateQuantity(userId, productId, quantity);
        return ResponseEntity.ok("Cart quantity updated");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestHeader("Authorization") String authHeader,
                                            @RequestParam String productId) {
        String userId = getUserIdFromHeader(authHeader);
        cartService.removeItem(userId, productId);
        return ResponseEntity.ok("Item removed from cart");
    }

    @GetMapping
    public ResponseEntity<?> listCartItems(@RequestHeader("Authorization") String authHeader) {
        String userId = getUserIdFromHeader(authHeader);
        Map<String, Object> cartInfo = cartService.listCartItemsWithTotal(userId);
        return ResponseEntity.ok(cartInfo);
    }

    @PostMapping("/voucher")
    public ResponseEntity<?> applyVoucher(@RequestHeader("Authorization") String authHeader,
                                          @RequestBody Map<String, String> payload) {
        String userId = getUserIdFromHeader(authHeader);
        String voucherCode = payload.get("voucherCode");
        if (voucherCode == null || voucherCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Voucher code is required."));
        }
        Map<String, Object> result = cartService.validateVoucher(voucherCode.trim(), userId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    private String getUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new RuntimeException("Invalid Authorization header");
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token))
            throw new RuntimeException("Invalid token");
        return jwtUtil.extractUserId(token, userService);
    }
}
