package com.example.ecommerce.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Quản lý danh sách sản phẩm yêu thích được lưu cục bộ trên thiết bị.
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

    /** Chuyển đổi trạng thái lưu sản phẩm yêu thích. */
    public static boolean toggle(Context context, String productId) {
        Set<String> favSet = new HashSet<>(prefs(context).getStringSet(KEY_IDS, new HashSet<>()));
        boolean isAddedFav = !favSet.remove(productId);
        if (isAddedFav) {
            favSet.add(productId);
        }
        prefs(context).edit().putStringSet(KEY_IDS, favSet).apply();
        return isAddedFav;
    }
}
