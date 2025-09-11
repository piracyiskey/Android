package org.convenient.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCart {
    @Id
    private String id; // UUID

    @Column(unique = true)
    private String userId;

    private String voucherCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}