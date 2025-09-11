package org.convenient.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String imageUrl;
    private String name;
    private String per;
    private int price;

    @Column(columnDefinition = "TEXT")
    private String desc;

    private boolean hot = false;
}
