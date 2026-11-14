package com.example.ecommerce.model;

/**
 * Thông tin tài khoản người dùng hệ thống.
 */
public class User {
    public String id;
    public String firstName;
    public String lastName;
    public String avatar;
    public String email;
    public String phoneNumber;
    public String role;

    /** Lấy tên hiển thị đầy đủ của tài khoản (Họ và Tên). */
    public String getFullName() {
        String uLastName = (lastName == null) ? "" : lastName;
        String uFirstName = (firstName == null) ? "" : firstName;
        String compositeName = (uLastName + " " + uFirstName).trim();
        return compositeName.isEmpty() ? email : compositeName;
    }
}
