package com.example.ecommerce.util;

import java.text.NumberFormat;
import java.util.Locale;

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
        return String.format(Locale.US, "%.1f", rating == null ? 0.0 : rating);
    }
}
