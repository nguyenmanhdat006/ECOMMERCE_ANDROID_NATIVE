package com.example.ecommerce.service;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.model.Review;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Danh sách các API của backend (xem docs/API_CONTRACT.md bên Ecommerce_BE).
 * Mỗi hàm tương ứng một API. Tham số @Query bằng null sẽ không được gửi lên.
 * Những API không cần dữ liệu trả về dùng Call<Void>.
 */
public interface ApiService {

    // ---------------------------------------------------------------- Đăng nhập, đăng ký

    @POST("api/auth/login")
    Call<AuthModels.LoginResponse> login(@Body AuthModels.LoginRequest body);

    @POST("api/auth/register")
    Call<AuthModels.ApiMessage> register(@Body AuthModels.RegisterRequest body);

    @POST("api/auth/verify")
    Call<Void> verify(@Body AuthModels.VerifyRequest body);

    // ---------------------------------------------------------------- Sản phẩm, danh mục

    @GET("api/products/search")
    Call<ProductPage> searchProducts(
            @Query("keyword") String keyword,
            @Query("categoryId") String categoryId,
            @Query("brand") String brand,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("minRating") Double minRating,
            @Query("isNewArrival") Boolean isNewArrival,
            @Query("sortBy") String sortBy,
            @Query("sortDirection") String sortDirection,
            @Query("page") int page,
            @Query("size") int size);

    @GET("api/products/{id}")
    Call<Product> getProduct(@Path("id") String id);

    @GET("api/category")
    Call<List<Category>> getCategories();

    // ---------------------------------------------------------------- Giỏ hàng (cần đăng nhập)

    @GET("api/carts/user/{userId}")
    Call<List<CartItem>> getCart(@Path("userId") String userId);

    @POST("api/carts")
    Call<Void> addToCart(@Body CartItem.Request body);

    @PUT("api/carts/{id}")
    Call<Void> updateCartItem(@Path("id") String id, @Body CartItem.Request body);

    @DELETE("api/carts/{id}")
    Call<Void> deleteCartItem(@Path("id") String id);

    // ---------------------------------------------------------------- Địa chỉ (cần đăng nhập)

    @GET("api/addresses/my-addresses")
    Call<List<Address>> getMyAddresses();

    @POST("api/addresses")
    Call<Address> createAddress(@Body Address body);

    @PUT("api/addresses/{id}")
    Call<Address> updateAddress(@Path("id") String id, @Body Address body);

    @DELETE("api/addresses/{id}")
    Call<Void> deleteAddress(@Path("id") String id);

    // ---------------------------------------------------------------- Đơn hàng (cần đăng nhập)

    @POST("api/orders")
    Call<Order> createOrder(@Body Order.Request body);

    @GET("api/orders/me")
    Call<List<Order>> getMyOrders();

    @GET("api/orders/{id}")
    Call<Order> getOrder(@Path("id") String id);

    @GET("api/orders/unreviewed")
    Call<List<Order.OrderItem>> getUnreviewedItems();

    // ---------------------------------------------------------------- Đánh giá

    @GET("api/reviews/product/{productId}")
    Call<List<Review>> getProductReviews(@Path("productId") String productId);

    @POST("api/reviews")
    Call<Review> createReview(@Body Review.Request body);

    @PUT("api/reviews/{id}")
    Call<Review> updateReview(@Path("id") String id, @Body Review.Request body);

    @DELETE("api/reviews/{id}")
    Call<Void> deleteReview(@Path("id") String id);
}
