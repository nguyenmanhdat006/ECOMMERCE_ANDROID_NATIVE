package com.example.ecommerce.util;

import android.net.Uri;

public final class ImageUrls {

    private ImageUrls() {
    }

    /**
     * Backend trả URL ảnh dạng http://localhost:8080/uploads/... ; emulator không truy cập được "localhost"
     * của máy tính nên đổi sang host của BASE_URL (10.0.2.2).
     */
    public static String fix(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            Uri base = Uri.parse(Constants.BASE_URL);
            return uri.buildUpon().encodedAuthority(base.getAuthority()).build().toString();
        }
        return url;
    }
}
