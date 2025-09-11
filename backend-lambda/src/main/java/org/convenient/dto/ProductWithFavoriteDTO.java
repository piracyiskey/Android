package org.convenient.dto;

import lombok.*;
import org.convenient.models.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductWithFavoriteDTO {
    private String id;
    private String name;
    private String per;
    private int price;
    private String desc;
    private String imageUrl;
    private boolean hot;
    private String categoryName;
    private boolean favorite;

    public ProductWithFavoriteDTO(Product product, boolean isFavorite) {
        this.id = product.getId();
        this.name = product.getName();
        this.per = product.getPer();
        this.price = product.getPrice();
        this.desc = product.getDesc();
        this.imageUrl = product.getImageUrl();
        this.hot = product.isHot();
        this.categoryName = product.getCategory().getName();
        this.favorite = isFavorite;
    }
}
