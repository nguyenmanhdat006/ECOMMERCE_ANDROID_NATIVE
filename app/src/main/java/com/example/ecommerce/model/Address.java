package com.example.ecommerce.model;

/** Địa chỉ giao hàng. Vừa dùng để nhận về, vừa dùng làm dữ liệu gửi lên khi thêm hoặc sửa. */
public class Address {
    public String id;
    public String name;          // tên gợi nhớ, ví dụ "Nhà riêng" (không bắt buộc)
    public String street;        // số nhà, đường
    public String city;          // thành phố
    public String state;         // tỉnh, quận
    public String zipCode;       // mã bưu chính
    public String phoneNumber;   // số điện thoại nhận hàng

    /** Dòng địa chỉ đầy đủ: "12 Nguyễn Trãi, Quận 1, Hồ Chí Minh". */
    public String getFullLine() {
        return street + ", " + state + ", " + city;
    }

    public String getDisplayName() {
        if (name == null || name.trim().isEmpty()) {
            return "Địa chỉ";
        }
        return name;
    }

    /** Chuỗi gửi vào trường shippingAddress của đơn hàng: "Nhà riêng - 0911222333 - 12 Nguyễn Trãi, ...". */
    public String toShippingText() {
        return getDisplayName() + " - " + phoneNumber + " - " + getFullLine();
    }
}
