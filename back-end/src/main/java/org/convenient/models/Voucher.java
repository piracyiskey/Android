package org.convenient.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Voucher {
    @Id
    private String id;

    private String vccode;

    @Column(name = "`desc`")
    private String desc;

    private int value;

    @Enumerated(EnumType.STRING)
    private DiscountType type;

    private int minTotal;
    private int quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;

    public enum DiscountType {
        PERCENTAGE, FIXED
    }
}
