package com.example.ecommerce.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Khớp với ProductDto của backend (GET /api/products/{id}, /api/products/search). */
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String brand;
    private Double rating;
    private String categoryId;
    private String categoryName;
    private String categoryTypeId;
    private String categoryTypeName;
    private String thumbnail;
    private String slug;
    private boolean newArrival;
    private List<ProductVariant> variants;
    private List<ProductResource> productResources;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getBrand() { return brand; }
    public Double getRating() { return rating; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryTypeName() { return categoryTypeName; }
    public String getThumbnail() { return thumbnail; }
    public String getSlug() { return slug; }
    public boolean isNewArrival() { return newArrival; }

    public List<ProductVariant> getVariants() {
        return variants == null ? new ArrayList<>() : variants;
    }

    public List<ProductResource> getProductResources() {
        return productResources == null ? new ArrayList<>() : productResources;
    }

    /** Tổng tồn kho của mọi biến thể. */
    public int getTotalStock() {
        int total = 0;
        for (ProductVariant v : getVariants()) {
            total += v.getStockQuantity();
        }
        return total;
    }

    /** Danh sách URL ảnh: ảnh chính đứng đầu; không có ảnh nào thì dùng thumbnail. */
    public List<String> getImageUrls() {
        List<String> urls = new ArrayList<>();
        for (ProductResource r : getProductResources()) {
            if (r.getUrl() != null && r.isPrimary()) {
                urls.add(r.getUrl());
            }
        }
        for (ProductResource r : getProductResources()) {
            if (r.getUrl() != null && !r.isPrimary()) {
                urls.add(r.getUrl());
            }
        }
        if (urls.isEmpty() && thumbnail != null) {
            urls.add(thumbnail);
        }
        return urls;
    }

    /** Ảnh đại diện cho thẻ sản phẩm. */
    public String getCoverUrl() {
        if (thumbnail != null && !thumbnail.isEmpty()) {
            return thumbnail;
        }
        List<String> urls = getImageUrls();
        return urls.isEmpty() ? null : urls.get(0);
    }

    /** Các size khác nhau (giữ thứ tự xuất hiện). */
    public List<String> getSizes() {
        Set<String> set = new LinkedHashSet<>();
        for (ProductVariant v : getVariants()) {
            if (v.getSize() != null) {
                set.add(v.getSize());
            }
        }
        return new ArrayList<>(set);
    }

    /** Các màu khác nhau (giữ thứ tự xuất hiện). */
    public List<String> getColors() {
        Set<String> set = new LinkedHashSet<>();
        for (ProductVariant v : getVariants()) {
            if (v.getColor() != null) {
                set.add(v.getColor());
            }
        }
        return new ArrayList<>(set);
    }
}
