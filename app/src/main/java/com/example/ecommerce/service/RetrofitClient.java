package com.example.ecommerce.service;

import com.example.ecommerce.BuildConfig;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tạo Retrofit một lần và dùng chung toàn app (singleton), thay cho dependency injection.
 * Mỗi yêu cầu gửi đi sẽ tự gắn token đăng nhập (nếu có) vào header Authorization.
 */
public final class RetrofitClient {

    private static ApiService apiService;

    private RetrofitClient() {
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .connectTimeout(Constants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(new TokenInterceptor());

            if (BuildConfig.DEBUG) {
                // Ghi log yêu cầu vào Logcat để dễ debug
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

    /** Tự thêm "Authorization: Bearer token" vào mọi yêu cầu khi đã đăng nhập. */
    private static class TokenInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String token = SessionManager.getToken();
            if (token != null) {
                request = request.newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .build();
            }
            return chain.proceed(request);
        }
    }
}
