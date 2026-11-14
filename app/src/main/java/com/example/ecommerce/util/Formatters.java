package com.example.ecommerce.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Các tiện ích định dạng dữ liệu hiển thị (Số tiền, Đánh giá, Ngày tháng).
 */
public final class Formatters {

    private Formatters() {
    }

    private static final NumberFormat VND =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    /** Định dạng số tiền sang đơn vị Việt Nam Đồng (VD: 150.000 ₫). */
    public static String vnd(double amount) {
        return VND.format(amount) + " ₫";
    }

    /** Định dạng điểm số đánh giá hiển thị (VD: 4.8). */
    public static String rating(Double rating) {
        double ratingVal = (rating == null) ? 0.0 : rating;
        return String.format(Locale.US, "%.1f", ratingVal);
    }

    /** Định dạng chuỗi ngày tháng ISO sang kiểu dd/MM/yyyy. */
    public static String date(String isoText) {
        if (isoText == null || isoText.length() < 10) {
            return "";
        }
        String yyyy = isoText.substring(0, 4);
        String MM = isoText.substring(5, 7);
        String dd = isoText.substring(8, 10);
        return dd + "/" + MM + "/" + yyyy;
    }
}
