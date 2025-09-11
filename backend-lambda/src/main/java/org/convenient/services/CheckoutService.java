package org.convenient.services;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.*;
import org.convenient.models.*;
import org.convenient.repository.*;
import org.convenient.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public void checkout(String token, CheckoutRequestDTO request) {
        String userId = jwtUtil.extractUserId(token, userService);
        User user = userRepository.findById(userId).orElseThrow();

        List<Cart> cartItems = cartRepository.findByUserId(userId);
        if (cartItems.isEmpty()) throw new RuntimeException("Cart is empty");

        int totalPrice = 0;
        Map<String, Product> productMap = new HashMap<>();

        for (Cart item : cartItems) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow();
            productMap.put(item.getProductId(), product);
            totalPrice += product.getPrice() * item.getQuantity();
        }

        String discount = null;
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByVccode(request.getVoucherCode())
                    .orElseThrow(() -> new RuntimeException("Invalid voucher"));

            if (!voucher.isActive() || voucher.getQuantity() <= 0 ||
                    LocalDateTime.now().isBefore(voucher.getStartDate()) ||
                    LocalDateTime.now().isAfter(voucher.getEndDate())) {
                throw new RuntimeException("Voucher not valid");
            }

            if (totalPrice >= voucher.getMinTotal()) {
                if (voucher.getType() == Voucher.DiscountType.PERCENTAGE) {
                    int percentOff = (totalPrice * voucher.getValue()) / 100;
                    totalPrice -= percentOff;
                } else { // FIXED
                    totalPrice -= voucher.getValue();
                }
                // Prevent negative prices
                totalPrice = Math.max(0, totalPrice);
                voucher.setQuantity(voucher.getQuantity() - 1);
                voucherRepository.save(voucher);
                discount = voucher.getVccode();
            }
        }

        Order order = new Order(
                generateUniqueOrderId(),
                userId,
                totalPrice,
                discount,
                request.getPayMethod(),
                "pending",
                LocalDateTime.now(),
                null
        );
        orderRepository.save(order);

        for (Cart item : cartItems) {
            Product product = productMap.get(item.getProductId());
            orderDetailRepository.save(new OrderDetail(
                    UUID.randomUUID().toString(),
                    order.getId(),
                    product.getId(),
                    product.getPrice(),
                    item.getQuantity()
            ));
        }

        cartRepository.deleteAll(cartItems);
    }



    private String generateUniqueOrderId() {
        Random random = new Random();
        String orderId;
        do {
            int num = 100000 + random.nextInt(900000); // ensure 6 digits
            orderId = String.format("%06d", num);
        } while (orderRepository.existsById(orderId));
        return orderId;
    }

    public List<OrderSummaryDTO> getUserOrders(String token) {
        String userId = jwtUtil.extractUserId(token, userService);
        List<Order> orders = orderRepository.findByUserId(userId);
        // Sort orders by createdDate in descending order (latest first)
        orders.sort(Comparator.comparing(Order::getCreatedDate).reversed());
        return orders.stream().map(order -> new OrderSummaryDTO(
                order.getId(),
                order.getTotalPrice(),
                order.getDiscountApplied(),
                order.getPayMethod(),
                order.getStatus(),
                order.getCreatedDate().toString()
        )).collect(Collectors.toList());
    }

    public Map<String, Object> getOrderDetails(String orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        List<String> productIds = details.stream()
                .map(OrderDetail::getProductId)
                .distinct()
                .toList();

        Map<String, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> itemList = new ArrayList<>();
        int totalPrice = 0;

        for (OrderDetail detail : details) {
            Product product = productMap.get(detail.getProductId());
            if (product == null) continue;

            int rowTotal = detail.getPriceAtPurchase() * detail.getQuantity();
            totalPrice += rowTotal;

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", product.getId());
            itemMap.put("productName", product.getName());
            itemMap.put("per", product.getPer());
            itemMap.put("priceAtPurchase", detail.getPriceAtPurchase());
            itemMap.put("quantity", detail.getQuantity());
            itemMap.put("imageUrl", product.getImageUrl());
            itemMap.put("total", rowTotal);

            itemList.add(itemMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("totalPrice", totalPrice);
        return result;
    }

    public Map<String, Object> getOrderInfo(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Map<String, Object> details = getOrderDetails(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", order.getId());
        result.put("totalPrice", order.getTotalPrice());
        result.put("payMethod", order.getPayMethod());
        result.put("discountApplied", order.getDiscountApplied());
        result.put("status", order.getStatus());
        result.put("createdDate", order.getCreatedDate().toString());
        result.put("SubTotal", details.get("totalPrice"));
        result.put("items", details.get("items"));
        return result;
    }

    // Add method to CheckoutService
    @Transactional
    public void deleteOrderById(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found");
        }
        orderDetailRepository.deleteByOrderId(orderId);
        orderRepository.deleteById(orderId);
    }




}