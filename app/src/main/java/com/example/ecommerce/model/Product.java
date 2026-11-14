package com.example.ecommerce.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Thông tin chi tiết sản phẩm kinh doanh.
 */
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

    /**
     * Kiểm tra có thông tin các phiên bản phân loại hay không.
     */
    public boolean hasStockInfo() {
        return variants != null;
    }

    /** Tính tổng lượng hàng tồn kho cho tất cả phiên bản phân loại. */
    public int getTotalStock() {
        int sumStock = 0;
        for (ProductVariant variantItem : getVariants()) {
            sumStock += variantItem.getStockQuantity();
        }
        return sumStock;
    }

    /** Lấy danh sách toàn bộ liên kết hình ảnh minh họa sản phẩm. */
    public List<String> getImageUrls() {
        List<String> mediaList = new ArrayList<>();
        for (ProductResource resItem : getProductResources()) {
            if (resItem.getUrl() != null && resItem.isPrimary()) {
                mediaList.add(resItem.getUrl());
            }
        }
        for (ProductResource resItem : getProductResources()) {
            if (resItem.getUrl() != null && !resItem.isPrimary()) {
                mediaList.add(resItem.getUrl());
            }
        }
        if (mediaList.isEmpty() && thumbnail != null) {
            mediaList.add(thumbnail);
        }
        return mediaList;
    }

    /** Lấy ảnh đại diện chính của sản phẩm. */
    public String getCoverUrl() {
        if (thumbnail != null && !thumbnail.isEmpty()) {
            return thumbnail;
        }
        List<String> allImages = getImageUrls();
        return allImages.isEmpty() ? null : allImages.get(0);
    }

    /** Trích xuất danh sách tất cả các kích cỡ có sẵn. */
    public List<String> getSizes() {
        Set<String> uniqueSizes = new LinkedHashSet<>();
        for (ProductVariant itemVariant : getVariants()) {
            if (itemVariant.getSize() != null) {
                uniqueSizes.add(itemVariant.getSize());
            }
        }
        return new ArrayList<>(uniqueSizes);
    }

    /** Trích xuất danh sách tất cả các màu sắc có sẵn. */
    public List<String> getColors() {
        Set<String> uniqueColors = new LinkedHashSet<>();
        for (ProductVariant itemVariant : getVariants()) {
            if (itemVariant.getColor() != null) {
                uniqueColors.add(itemVariant.getColor());
            }
        }
        return new ArrayList<>(uniqueColors);
    }
}
