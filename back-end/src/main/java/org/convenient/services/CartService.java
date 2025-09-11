package org.convenient.services;

import lombok.RequiredArgsConstructor;
import org.convenient.dto.ProductDTO;
import org.convenient.models.Cart;
import org.convenient.models.Product;
import org.convenient.models.Voucher;
import org.convenient.repository.CartRepository;
import org.convenient.repository.ProductRepository;
import org.convenient.repository.VoucherRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;


    public int getDistinctProductCount(String userId) {
        return cartRepository.findByUserId(userId).size();
    }

    public void addOrUpdateItem(String userId, String productId, int quantity) {
        Optional<Cart> existingCartOpt = cartRepository.findByUserIdAndProductId(userId, productId);
        if (existingCartOpt.isPresent()) {
            Cart existing = existingCartOpt.get();
            existing.setQuantity(existing.getQuantity() + quantity);
            cartRepository.save(existing);
        } else {
            Cart cart = new Cart();
            cart.setId(UUID.randomUUID().toString());
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cartRepository.save(cart);
        }
    }

    public void updateQuantity(String userId, String productId, int newQuantity) {
        cartRepository.findByUserIdAndProductId(userId, productId).ifPresent(cart -> {
            cart.setQuantity(newQuantity);
            cartRepository.save(cart);
        });
    }

    public void removeItem(String userId, String productId) {
        cartRepository.findByUserIdAndProductId(userId, productId).ifPresent(cartRepository::delete);
    }

    public List<ProductDTO> listCartItems(String userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        return cartItems.stream()
                .map(cart -> {
                    Product product = productRepository.findById(cart.getProductId()).orElse(null);
                    if (product == null) return null;
                    ProductDTO dto = new ProductDTO(product);
                    dto.setDesc(dto.getDesc() + " (x" + cart.getQuantity() + ")");
                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    public Map<String, Object> listCartItemsWithTotal(String userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        List<Map<String, Object>> itemList = new ArrayList<>();
        int totalPrice = 0;

        for (Cart cart : cartItems) {
            Product product = productRepository.findById(cart.getProductId()).orElse(null);
            if (product == null) continue;

            int rowTotal = product.getPrice() * cart.getQuantity();
            totalPrice += rowTotal;

            Map<String, Object> itemMap = new HashMap<>();
            ProductDTO dto = new ProductDTO(product);
            dto.setDesc(dto.getDesc() + " (x" + cart.getQuantity() + ")");

            itemMap.put("product", dto);
            itemMap.put("quantity", cart.getQuantity());
            itemMap.put("total", rowTotal);

            itemList.add(itemMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("totalPrice", totalPrice);
        return result;
    }

    public Map<String, Object> validateVoucher(String voucherCode, String userId) {
        Optional<Voucher> optVoucher = voucherRepository.findByVccode(voucherCode);
        if (optVoucher.isEmpty()) {
            return Map.of("error", "Voucher does not exist.");
        }

        Voucher voucher = optVoucher.get();
        LocalDateTime now = LocalDateTime.now();

        if (!voucher.isActive()) {
            return Map.of("error", "Voucher is no longer active.");
        }
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return Map.of("error", "Voucher is not valid yet.");
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return Map.of("error", "Voucher is expired.");
        }
        if (voucher.getQuantity() <= 0) {
            return Map.of("error", "Voucher is out of stock.");
        }

        Map<String, Object> cartInfo = listCartItemsWithTotal(userId);
        int totalPrice = (int) cartInfo.getOrDefault("totalPrice", 0);

        if (totalPrice < voucher.getMinTotal()) {
            return Map.of("error", "Cart total is below the voucher minimum required: " + voucher.getMinTotal());
        }

        return Map.of(
                "type", voucher.getType().name(),
                "value", voucher.getValue()
        );
    }

}
