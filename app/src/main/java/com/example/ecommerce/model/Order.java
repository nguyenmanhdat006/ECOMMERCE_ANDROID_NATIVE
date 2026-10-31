package com.example.ecommerce.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Đơn hàng (OrderResponse của backend).
 * status: PENDING, SHIPPING, WAIT_DELIVER, PAID, CANCELED, REFUND.
 */
public class Order {
    public String id;
    public String orderNumber;
    public String orderDate;
    public double totalAmount;
    public String status;
    public String paymentStatus;
    public String paymentMethod;
    public String shippingAddress;
    public String notes;
    public String customerName;
    public List<OrderItem> orderItems = new ArrayList<>();

    /** Một dòng sản phẩm trong đơn. */
    public static class OrderItem {
        public String id;
        public int quantity;
        public double unitPrice;
        public double totalPrice;
        public String productId;
        public String productVariantId;
        public String productName;
        public String thumbnail;

        /** Đã đánh giá chưa (JSON có thể là "isReviewed" hoặc "reviewed"). */
        @SerializedName(value = "isReviewed", alternate = {"reviewed"})
        public boolean reviewed;
    }

    /** Dữ liệu gửi lên khi tạo đơn (POST /api/orders). */
    public static class Request {
        public String shippingAddress;
        public String paymentMethod = "COD";
        public String notes;
        public List<Line> orderItems = new ArrayList<>();
    }

    /** Một dòng hàng khi tạo đơn: chỉ cần id biến thể và số lượng, giá do máy chủ tính. */
    public static class Line {
        public String productVariantId;
        public int quantity;

        public Line(String productVariantId, int quantity) {
            this.productVariantId = productVariantId;
            this.quantity = quantity;
        }
    }
}
