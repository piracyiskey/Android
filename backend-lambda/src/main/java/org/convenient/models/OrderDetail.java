package org.convenient.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OrderDetail {
    @Id
    private String id;

    private String orderId;
    private String productId;
    private int priceAtPurchase;
    private int quantity;
}