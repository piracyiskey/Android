package org.convenient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderInfoDTO {
    private String orderId;
    private int totalPrice;
    private String payMethod;
    private String discountApplied;
    private String status;
    private String createdDate;
    private List<OrderDetailDTO> items;
}
