package org.convenient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.convenient.models.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private String per;
    private int price;
    private String desc;
    private String imageUrl;
    private boolean hot;
    private String categoryName;

    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.per = product.getPer();
        this.price = product.getPrice();
        this.desc = product.getDesc();
        this.imageUrl = product.getImageUrl();
        this.hot = product.isHot();
        this.categoryName = product.getCategory().getName();
    }
}
