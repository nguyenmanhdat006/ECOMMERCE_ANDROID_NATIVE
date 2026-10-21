package com.example.ecommerce.util;

import com.example.ecommerce.BuildConfig;

public final class Constants {

    private Constants() {
    }

    /** Cấu hình trong app/build.gradle.kts (buildConfigField). */
    public static final String BASE_URL = BuildConfig.BASE_URL;
    /** true = dùng dữ liệu giả (không cần backend). Đổi trong app/build.gradle.kts. */
    public static final boolean USE_MOCK_DATA = BuildConfig.USE_MOCK_DATA;
    public static final long CONNECT_TIMEOUT_SECONDS = 15L;
    public static final int PAGE_SIZE = 10;

    /** Khóa dữ liệu truyền giữa các màn hình. */
    public static final String ARG_PRODUCT_ID = "productId";
}
