package org.convenient.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_product_id", columnList = "productId"),
        @Index(name = "idx_user_product", columnList = "userId, productId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    private String id;

    private String userId;

    private String productId;

    private int quantity;
}
