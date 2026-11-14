package com.example.ecommerce.model;

/**
 * Đố tượng chứa thông số bộ lọc và điều kiện tìm kiếm sản phẩm.
 */
public class ProductFilter {
    public String keyword;
    public String categoryId;
    public String brand;
    public Double minPrice;
    public Double maxPrice;
    public Double minRating;
    public boolean onlyNew;
    public String sortBy = "name";        // Tiêu chí sắp xếp: name, price, rating
    public String sortDirection = "asc";  // Hướng sắp xếp: asc, desc

    /** Đếm tổng số tiêu chí lọc nâng cao đang được áp dụng. */
    public int countActive() {
        int activeFilterCount = 0;
        if (brand != null && !brand.isEmpty()) activeFilterCount++;
        if (minPrice != null) activeFilterCount++;
        if (maxPrice != null) activeFilterCount++;
        if (minRating != null) activeFilterCount++;
        if (onlyNew) activeFilterCount++;
        return activeFilterCount;
    }

    /** Xóa bỏ toàn bộ các điều kiện lọc nâng cao. */
    public void clearAdvanced() {
        brand = null;
        minPrice = null;
        maxPrice = null;
        minRating = null;
        onlyNew = false;
    }
}
