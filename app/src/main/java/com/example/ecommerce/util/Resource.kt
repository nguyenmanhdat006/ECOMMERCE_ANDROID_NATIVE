package com.example.ecommerce.util

// Kết quả của một lần gọi dữ liệu: thành công hoặc lỗi kèm thông báo để hiển thị
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
