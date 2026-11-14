package com.example.ecommerce.model;

/**
 * Đại diện cho một mục sản phẩm có trong giỏ hàng.
 */
public class CartItem {
    public String id;
    public Product product;              // Thông tin chung của sản phẩm
    public ProductVariant productVariant; // Phân loại chọn mua (màu sắc, kích cỡ)
    public int quantity;
    public String userId;

    /** Trả về chuỗi tổng hợp thông tin kích cỡ và màu sắc. */
    public String getVariantText() {
        if (productVariant == null) {
            return "";
        }
        return productVariant.getSize() + ", " + productVariant.getColor();
    }

    /** Trả về nhãn phân loại hiển thị trên thẻ giỏ hàng (Màu trước, Size sau). */
    public String getVariantLabel() {
        if (productVariant == null) {
            return "";
        }
        String cVal = productVariant.getColor();
        String sVal = productVariant.getSize();
        boolean isColorPresent = cVal != null && !cVal.isEmpty();
        boolean isSizePresent = sVal != null && !sVal.isEmpty();
        if (isColorPresent && isSizePresent) {
            return cVal + ", " + sVal;
        }
        return isColorPresent ? cVal : (isSizePresent ? sVal : "");
    }

    /** Thành tiền cho sản phẩm trong giỏ = đơn giá * số lượng. */
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    /** Đơn vị dữ liệu yêu cầu khi thay đổi mục giỏ hàng. */
    public static class Request {
        public String productVariantId;
        public int quantity;

        public Request(String productVariantId, int quantity) {
            this.productVariantId = productVariantId;
            this.quantity = quantity;
        }
    }
}
