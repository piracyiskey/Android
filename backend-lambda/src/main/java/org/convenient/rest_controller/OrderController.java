package org.convenient.rest_controller;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.*;
import org.convenient.services.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("Authorization") String token,
                                      @RequestBody CheckoutRequestDTO request) {
        checkoutService.checkout(token.replace("Bearer ", ""), request);
        return ResponseEntity.ok("Order placed successfully");
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryDTO>> getUserOrders(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(checkoutService.getUserOrders(token.replace("Bearer ", "")));
    }

    @GetMapping("/{orderId}/details")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable String orderId) {
        return ResponseEntity.ok(checkoutService.getOrderDetails(orderId));
    }

    @GetMapping("/{orderId}/info")
    public ResponseEntity<Map<String, Object>> getOrderInfo(@PathVariable String orderId) {
        return ResponseEntity.ok(checkoutService.getOrderInfo(orderId));
    }


    // Add this method to your OrderController
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable String orderId) {
        try {
            checkoutService.deleteOrderById(orderId);
            return ResponseEntity.ok("Order deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}