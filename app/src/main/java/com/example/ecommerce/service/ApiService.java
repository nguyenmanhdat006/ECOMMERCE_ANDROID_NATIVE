package com.example.ecommerce.service;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductPage;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface Retrofit: mỗi hàm là một API của backend (xem docs/API_CONTRACT.md bên Ecommerce_BE).
 * Tham số null sẽ không được gửi lên.
 */
public interface ApiService {

    @GET("api/products/search")
    Call<ProductPage> searchProducts(
            @Query("keyword") String keyword,
            @Query("categoryId") String categoryId,
            @Query("sortBy") String sortBy,
            @Query("sortDirection") String sortDirection,
            @Query("page") int page,
            @Query("size") int size);

    @GET("api/products/{id}")
    Call<Product> getProduct(@Path("id") String id);

    @GET("api/category")
    Call<List<Category>> getCategories();
}
