package com.example.ecommerce.service

import com.example.ecommerce.model.Product
import retrofit2.http.GET

// Interface Retrofit: mỗi hàm là một API của backend (xem docs/API_CONTRACT.md bên Ecommerce_BE)
interface ApiService {

    @GET("api/products")
    suspend fun getProducts(): List<Product>
}
