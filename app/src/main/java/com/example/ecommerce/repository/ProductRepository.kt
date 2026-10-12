package com.example.ecommerce.repository

import com.example.ecommerce.model.Product
import com.example.ecommerce.service.ApiService
import com.example.ecommerce.util.Resource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Lớp truy cập dữ liệu: ViewModel chỉ làm việc với repository, không biết dữ liệu đến từ API hay Room
@Singleton
class ProductRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getProducts(): Resource<List<Product>> = try {
        Resource.Success(api.getProducts())
    } catch (e: HttpException) {
        Resource.Error("Lỗi máy chủ (${e.code()})")
    } catch (e: IOException) {
        Resource.Error("Không kết nối được máy chủ, kiểm tra backend và mạng")
    }
}
