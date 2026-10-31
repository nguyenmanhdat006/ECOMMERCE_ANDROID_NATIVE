package com.example.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

/** Danh mục sản phẩm (GET /api/category), kèm các loại danh mục con. */
public class Category {
    private String id;
    private String name;
    private String code;
    private String description;
    private List<CategoryType> categoryTypes;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    public List<CategoryType> getCategoryTypes() {
        return categoryTypes == null ? new ArrayList<>() : categoryTypes;
    }

    /** Loại danh mục con, ví dụ danh mục "Thời trang nam" có loại "Áo thun". */
    public static class CategoryType {
        public String id;
        public String name;
        public String code;
    }
}
