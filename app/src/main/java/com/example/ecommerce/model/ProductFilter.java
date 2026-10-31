package com.example.ecommerce.model;

/**
 * Gom các điều kiện tìm kiếm và lọc sản phẩm vào một chỗ.
 * Trường nào null (hoặc false) nghĩa là không lọc theo điều kiện đó.
 */
public class ProductFilter {
    public String keyword;
    public String categoryId;
    public String brand;
    public Double minPrice;
    public Double maxPrice;
    public Double minRating;
    public boolean onlyNew;
    public String sortBy = "name";        // name, price, rating
    public String sortDirection = "asc";  // asc, desc

    /** Số bộ lọc nâng cao đang bật (hiện trên nút Lọc). Không tính từ khóa, danh mục, sắp xếp. */
    public int countActive() {
        int count = 0;
        if (brand != null && !brand.isEmpty()) count++;
        if (minPrice != null) count++;
        if (maxPrice != null) count++;
        if (minRating != null) count++;
        if (onlyNew) count++;
        return count;
    }

    /** Xóa các bộ lọc nâng cao (giữ lại từ khóa, danh mục, sắp xếp). */
    public void clearAdvanced() {
        brand = null;
        minPrice = null;
        maxPrice = null;
        minRating = null;
        onlyNew = false;
    }
}
