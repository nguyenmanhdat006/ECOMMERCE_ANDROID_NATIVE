package com.example.ecommerce.model;

/**
 * Thông tin bài đánh giá sản phẩm của người mua.
 */
public class Review {
    public String id;
    public int rating;        // Điểm đánh giá (1-5 sao)
    public String comment;
    public String createdAt;
    public String updatedAt;
    public Reviewer user;

    public transient String orderItemId;

    /** Thông tin người viết phản hồi đánh giá. */
    public static class Reviewer {
        public String id;
        public String firstName;
        public String lastName;
        public String avatar;

        public String getFullName() {
            String surname = (lastName == null) ? "" : lastName;
            String givenName = (firstName == null) ? "" : firstName;
            return (surname + " " + givenName).trim();
        }
    }

    /** Đơn vị dữ liệu gửi yêu cầu tạo/sửa đánh giá. */
    public static class Request {
        public String productId;
        public String orderItemId;
        public int rating;
        public String comment;
    }
}
