package com.example.ecommerce.model;

/**
 * Các đối tượng gửi lên và nhận về của nhóm API đăng nhập / đăng ký.
 * Gom chung một file vì mỗi lớp rất nhỏ. Tên trường phải trùng JSON của backend.
 */
public class AuthModels {

    /** Gửi lên khi đăng nhập (POST /api/auth/login). userName chính là email. */
    public static class LoginRequest {
        public String userName;
        public String password;

        public LoginRequest(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }

    /** Nhận về khi đăng nhập thành công: token và thông tin người dùng. */
    public static class LoginResponse {
        public String token;
        public User user;
    }

    /** Gửi lên khi đăng ký (POST /api/auth/register). */
    public static class RegisterRequest {
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public String phoneNumber;
    }

    /** Gửi lên khi nhập mã xác thực email (POST /api/auth/verify). */
    public static class VerifyRequest {
        public String userName;
        public String code;

        public VerifyRequest(String userName, String code) {
            this.userName = userName;
            this.code = code;
        }
    }

    /** Thông báo đơn giản backend trả về khi đăng ký: { "code": 200, "message": "User created!" }. */
    public static class ApiMessage {
        public int code;
        public String message;
    }
}
