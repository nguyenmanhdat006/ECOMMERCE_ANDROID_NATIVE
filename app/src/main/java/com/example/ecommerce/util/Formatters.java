package com.example.ecommerce.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Các hàm định dạng chữ để hiển thị (giá tiền, điểm, ngày). */
public final class Formatters {

    private Formatters() {
    }

    private static final NumberFormat VND =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    /** 259000 -> "259.000 ₫" */
    public static String vnd(double amount) {
        return VND.format(amount) + " ₫";
    }

    /** 4.5 -> "4.5"; null -> "0.0" */
    public static String rating(Double rating) {
        double value = (rating == null) ? 0.0 : rating;
        return String.format(Locale.US, "%.1f", value);
    }

    /** "2026-10-12T21:11:35.107+00:00" -> "12/10/2026" (lấy 10 ký tự đầu yyyy-MM-dd). */
    public static String date(String isoText) {
        if (isoText == null || isoText.length() < 10) {
            return "";
        }
        String year = isoText.substring(0, 4);
        String month = isoText.substring(5, 7);
        String day = isoText.substring(8, 10);
        return day + "/" + month + "/" + year;
    }
}
