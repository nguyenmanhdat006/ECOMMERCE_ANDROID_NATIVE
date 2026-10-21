package com.example.ecommerce.model;

/** Biến thể của sản phẩm: một cặp màu + size cùng tồn kho. */
public class ProductVariant {
    private String id;
    private String color;
    private String size;
    private int stockQuantity;

    public String getId() { return id; }
    public String getColor() { return color; }
    public String getSize() { return size; }
    public int getStockQuantity() { return stockQuantity; }
}
