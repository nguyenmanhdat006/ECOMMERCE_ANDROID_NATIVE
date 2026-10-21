package com.example.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

/** Kết quả phân trang của GET /api/products/search. */
public class ProductPage {
    private int totalItems;
    private int totalPages;
    private int pageSize;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<Product> products;

    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public int getPageSize() { return pageSize; }
    public int getCurrentPage() { return currentPage; }
    public boolean hasNext() { return hasNext; }
    public boolean hasPrevious() { return hasPrevious; }

    public List<Product> getProducts() {
        return products == null ? new ArrayList<>() : products;
    }
}
