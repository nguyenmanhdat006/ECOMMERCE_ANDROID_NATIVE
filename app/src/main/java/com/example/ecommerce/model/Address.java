package com.example.ecommerce.model;

/**
 * Thực thể thông tin địa chỉ giao nhận hàng của người dùng.
 */
public class Address {
    public String id;
    public String name;          // Nhãn địa chỉ (VD: "Văn phòng", "Nhà riêng")
    public String street;        // Số nhà, tên đường
    public String city;          // Thành phố / Tỉnh
    public String state;         // Quận / Huyện
    public String zipCode;       // Mã bưu điện
    public String phoneNumber;   // Số điện thoại liên hệ nhận hàng

    /** Trả về chuỗi địa chỉ chi tiết định dạng đầy đủ. */
    public String getFullLine() {
        return street + ", " + state + ", " + city;
    }

    /** Trả về tên hiển thị của địa chỉ, mặc định là 'Địa chỉ' nếu trống. */
    public String getDisplayName() {
        if (name == null || name.trim().isEmpty()) {
            return "Địa chỉ";
        }
        return name;
    }

    /** Chuỗi tổng hợp dùng để ghi nhận địa chỉ giao hàng trên đơn hàng. */
    public String toShippingText() {
        return getDisplayName() + " - " + phoneNumber + " - " + getFullLine();
    }
}
