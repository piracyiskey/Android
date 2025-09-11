package org.convenient.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "`order`") // Escaping the table name
@Getter @Setter
@NoArgsConstructor
public class Order {
    @Id
    private String id;

    private String userId;
    private int totalPrice;
    private String discountApplied;
    private String payMethod;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updateDate;


    public Order(String id, String userId, int totalPrice, String discount, String payMethod, String status, LocalDateTime createdDate, LocalDateTime updateDate) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.discountApplied = discount;
        this.payMethod = payMethod;
        this.status = status;
        this.createdDate = createdDate;
        this.updateDate = updateDate;
    }
}
