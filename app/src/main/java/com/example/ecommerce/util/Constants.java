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

    /** Chỉ hỗ trợ thanh toán khi nhận hàng. */
    public static final String PAYMENT_COD = "COD";

    // Khóa dùng để truyền dữ liệu giữa các màn hình (Bundle arguments)
    public static final String ARG_PRODUCT_ID = "productId";
    public static final String ARG_PRODUCT_NAME = "productName";
    public static final String ARG_CATEGORY_ID = "categoryId";
    public static final String ARG_CATEGORY_NAME = "categoryName";
    public static final String ARG_KEYWORD = "keyword";
    public static final String ARG_EMAIL = "email";
    public static final String ARG_ORDER_ID = "orderId";
    public static final String ARG_ORDER_NUMBER = "orderNumber";
    public static final String ARG_ORDER_TOTAL = "orderTotal";
    public static final String ARG_SUCCESS = "success";
    public static final String ARG_SELECT_MODE = "selectMode";
    public static final String ARG_ADDRESS_JSON = "addressJson";
    public static final String ARG_ORDER_ITEM_ID = "orderItemId";
    public static final String ARG_IMAGE_URL = "imageUrl";
    public static final String ARG_REVIEW_ID = "reviewId";
    public static final String ARG_RATING = "rating";
    public static final String ARG_COMMENT = "comment";
    public static final String ARG_MESSAGE = "message";
}
