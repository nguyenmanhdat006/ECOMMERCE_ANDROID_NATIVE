package com.example.ecommerce.model;

/** Người dùng đăng nhập (UserDto của backend). Các trường để public cho dễ đọc. */
public class User {
    public String id;
    public String firstName;
    public String lastName;
    public String avatar;
    public String email;
    public String phoneNumber;
    public String role;

    /** Tên đầy đủ kiểu Việt Nam: họ + tên. Nếu chưa có thì dùng email. */
    public String getFullName() {
        String last = (lastName == null) ? "" : lastName;
        String first = (firstName == null) ? "" : firstName;
        String name = (last + " " + first).trim();
        return name.isEmpty() ? email : name;
    }
}
