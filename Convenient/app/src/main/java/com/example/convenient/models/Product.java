package com.example.convenient.models;

public class Product {
    private String id;
    private String name;
    private String per;
    private int price;
    private String desc;
    private String imageUrl;
    private boolean hot;
    private String categoryName;
    private boolean favorite;

    public Product() {}

    public Product(String id, String name, String per, int price, String desc,
                   String imageUrl, boolean hot, String categoryName, boolean favorite) {
        this.id = id;
        this.name = name;
        this.per = per;
        this.price = price;
        this.desc = desc;
        this.imageUrl = imageUrl;
        this.hot = hot;
        this.categoryName = categoryName;
        this.favorite = favorite;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPer() {
        return per;
    }

    public int getPrice() {
        return price;
    }

    public String getDesc() {
        return desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isHot() {
        return hot;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public boolean isFavorite() {
        return favorite;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPer(String per) {
        this.per = per;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setHot(boolean hot) {
        this.hot = hot;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
