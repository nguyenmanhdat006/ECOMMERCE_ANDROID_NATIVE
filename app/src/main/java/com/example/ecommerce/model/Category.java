package com.example.ecommerce.model;

/** Danh mục sản phẩm (GET /api/category). */
public class Category {
    private String id;
    private String name;
    private String code;
    private String description;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
}
