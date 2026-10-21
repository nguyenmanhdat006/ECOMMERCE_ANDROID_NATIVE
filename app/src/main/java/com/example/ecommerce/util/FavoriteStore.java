package com.example.ecommerce.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Lưu danh sách sản phẩm yêu thích ngay trên máy (backend chưa có API yêu thích).
 */
public final class FavoriteStore {

    private static final String PREFS = "favorites";
    private static final String KEY_IDS = "ids";

    private FavoriteStore() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isFavorite(Context context, String productId) {
        return prefs(context).getStringSet(KEY_IDS, new HashSet<>()).contains(productId);
    }

    /** Đảo trạng thái yêu thích, trả về trạng thái mới. */
    public static boolean toggle(Context context, String productId) {
        Set<String> ids = new HashSet<>(prefs(context).getStringSet(KEY_IDS, new HashSet<>()));
        boolean nowFavorite = !ids.remove(productId);
        if (nowFavorite) {
            ids.add(productId);
        }
        prefs(context).edit().putStringSet(KEY_IDS, ids).apply();
        return nowFavorite;
    }
}
