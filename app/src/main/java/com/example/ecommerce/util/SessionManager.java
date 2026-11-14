package com.example.ecommerce.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ecommerce.model.User;
import com.google.gson.Gson;

/**
 * Quản lý phiên làm việc và thông tin đăng nhập của người dùng.
 */
public final class SessionManager {

    private static final String PREFS_NAME = "session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER = "user";

    private static SharedPreferences prefs;
    private static final Gson GSON = new Gson();

    private SessionManager() {
    }

    public static void init(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

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
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson == null) {
            return null;
        }
        return GSON.fromJson(userJson, User.class);
    }

    public static void logout() {
        prefs.edit().clear().apply();
    }
}
