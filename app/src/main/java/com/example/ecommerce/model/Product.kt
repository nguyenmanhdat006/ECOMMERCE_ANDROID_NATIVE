package com.example.ecommerce.model

import com.google.gson.annotations.SerializedName

// Khớp với ProductDto của backend (GET /api/products)
data class Product(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val brand: String? = null,
    val rating: Float? = null,
    val thumbnail: String? = null,
    val slug: String? = null,
    val categoryName: String? = null,
    @SerializedName("newArrival") val isNewArrival: Boolean? = null,
    val variants: List<ProductVariant> = emptyList()
)

data class ProductVariant(
    val id: String,
    val color: String,
    val size: String,
    val stockQuantity: Int
)
