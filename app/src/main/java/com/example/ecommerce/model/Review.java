package com.example.ecommerce.model;

/** Đánh giá của một người dùng về sản phẩm (GET /api/reviews/product/{id}). */
public class Review {
    public String id;
    public int rating;        // 1 đến 5 sao
    public String comment;
    public String createdAt;
    public String updatedAt;
    public Reviewer user;

    /** Id dòng đơn đã đánh giá. Chỉ dùng cho dữ liệu giả (transient: không gửi, không nhận qua JSON). */
    public transient String orderItemId;

    /** Người viết đánh giá (backend chỉ trả tên và ảnh đại diện). */
    public static class Reviewer {
        public String id;
        public String firstName;
        public String lastName;
        public String avatar;

        public String getFullName() {
            String last = (lastName == null) ? "" : lastName;
            String first = (firstName == null) ? "" : firstName;
            return (last + " " + first).trim();
        }
    }

    /** Dữ liệu gửi lên khi viết (cần productId, orderItemId) hoặc sửa (chỉ cần rating, comment) đánh giá. */
    public static class Request {
        public String productId;
        public String orderItemId;
        public int rating;
        public String comment;
    }
}
