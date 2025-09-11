package com.example.convenient.models;

public class OrderItem {
    private String productId;
    private String productName;
    private String imageUrl;
    private String per;
    private int quantity;
    private int total;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPer() { return per; }

    public void setPer(String per) { this.per = per; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}
