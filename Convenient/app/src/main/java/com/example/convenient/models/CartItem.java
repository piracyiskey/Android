package com.example.convenient.models;

public class CartItem {
    private Product product;
    private int quantity;
    private int total;

    public CartItem() {}

    public CartItem(Product product, int quantity, int total) {
        this.product = product;
        this.quantity = quantity;
        this.total = total;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
