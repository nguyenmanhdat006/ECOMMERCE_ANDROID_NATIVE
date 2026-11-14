package com.example.ecommerce.util;

import android.net.Uri;

/**
 * Tiện ích xử lý địa chỉ đường dẫn hình ảnh từ backend.
 */
public final class ImageUrls {

    private ImageUrls() {
    }

    /** Chuẩn hóa địa chỉ URL ảnh phục vụ hiển thị trên môi trường Emulator/Thiết bị. */
    public static String fix(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Uri parsedUri = Uri.parse(url);
        String hostStr = parsedUri.getHost();
        if ("localhost".equals(hostStr) || "127.0.0.1".equals(hostStr)) {
            Uri baseUri = Uri.parse(Constants.BASE_URL);
            return parsedUri.buildUpon().encodedAuthority(baseUri.getAuthority()).build().toString();
        }
        return url;
    }
}
