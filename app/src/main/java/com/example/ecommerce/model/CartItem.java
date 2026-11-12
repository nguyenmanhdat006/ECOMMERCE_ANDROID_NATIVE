package com.example.ecommerce.model;

/** Một dòng trong giỏ hàng (GET /api/carts/user/{userId}). */
public class CartItem {
    public String id;
    public Product product;              // thông tin sản phẩm (tên, giá, ảnh)
    public ProductVariant productVariant; // biến thể đã chọn (size, màu, tồn kho)
    public int quantity;
    public String userId;

    /** Chữ mô tả biến thể, ví dụ "M, Trắng". */
    public String getVariantText() {
        if (productVariant == null) {
            return "";
        }
        return productVariant.getSize() + ", " + productVariant.getColor();
    }

    /** Nhãn theo thiết kế giỏ hàng: màu trước, size sau, ví dụ "Trắng, M". */
    public String getVariantLabel() {
        if (productVariant == null) {
            return "";
        }
        String color = productVariant.getColor();
        String size = productVariant.getSize();
        boolean hasColor = color != null && !color.isEmpty();
        boolean hasSize = size != null && !size.isEmpty();
        if (hasColor && hasSize) {
            return color + ", " + size;
        }
        return hasColor ? color : (hasSize ? size : "");
    }

    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    /** Dữ liệu gửi lên khi thêm hoặc sửa số lượng dòng giỏ hàng. */
    public static class Request {
        public String productVariantId;
        public int quantity;

        public Request(String productVariantId, int quantity) {
            this.productVariantId = productVariantId;
            this.quantity = quantity;
        }
    }
}
