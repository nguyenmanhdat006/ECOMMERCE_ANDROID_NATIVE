package com.example.ecommerce.util

import java.text.NumberFormat
import java.util.Locale

// 259000.0 -> "259.000 ₫"
fun Double.toVnd(): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(this) + " ₫"
