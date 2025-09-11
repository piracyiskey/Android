package org.convenient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private String productId;
    private String productName;
    private int priceAtPurchase;
    private int quantity;
    private String imageUrl;
}