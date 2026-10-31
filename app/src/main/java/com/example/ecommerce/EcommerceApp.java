package com.example.ecommerce;

import android.app.Application;

import com.example.ecommerce.util.SessionManager;

/**
 * Lớp Application: chạy một lần khi app khởi động, trước mọi màn hình.
 * Ở đây ta chuẩn bị SessionManager (nơi lưu token đăng nhập).
 */
public class EcommerceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(this);
    }
}
