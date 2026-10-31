package com.example.ecommerce.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ecommerce.model.User;
import com.google.gson.Gson;

/**
 * Lưu phiên đăng nhập (token và thông tin người dùng) vào bộ nhớ máy,
 * để lần sau mở app vẫn còn đăng nhập.
 *
 * Cách dùng ở bất kỳ đâu:
 *   SessionManager.isLoggedIn()   -> đã đăng nhập chưa
 *   SessionManager.getUser()      -> thông tin người dùng (null nếu chưa đăng nhập)
 *   SessionManager.logout()       -> đăng xuất
 */
public final class SessionManager {

    private static final String PREFS_NAME = "session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";

    private static SharedPreferences prefs;
    private static final Gson GSON = new Gson();

    private SessionManager() {
    }

    /** Gọi một lần trong EcommerceApp.onCreate(). */
    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Lưu thông tin sau khi đăng nhập thành công. */
    public static void saveLogin(String token, User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, GSON.toJson(user))
                .apply();
    }

    public static String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public static boolean isLoggedIn() {
        return getToken() != null;
    }

    public static User getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) {
            return null;
        }
        return GSON.fromJson(json, User.class);
    }

    public static void logout() {
        prefs.edit().clear().apply();
    }
}
