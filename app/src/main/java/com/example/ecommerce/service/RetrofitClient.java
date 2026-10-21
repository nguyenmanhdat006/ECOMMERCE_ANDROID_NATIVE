package com.example.ecommerce.service;

import com.example.ecommerce.BuildConfig;
import com.example.ecommerce.util.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tạo Retrofit một lần và dùng chung toàn app (singleton), thay cho dependency injection.
 * Khi làm đăng nhập: thêm interceptor gắn header "Authorization: Bearer token" vào OkHttpClient ở đây.
 */
public final class RetrofitClient {

    private static ApiService apiService;

    private RetrofitClient() {
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
                client.addInterceptor(logging);
            }
            apiService = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return apiService;
    }
}
