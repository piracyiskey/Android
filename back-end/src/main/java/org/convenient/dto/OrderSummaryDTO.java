package org.convenient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {
    private String orderId;
    private int totalPrice;
    private String discountApplied;
    private String payMethod;
    private String status;
    private String createdDate;
}