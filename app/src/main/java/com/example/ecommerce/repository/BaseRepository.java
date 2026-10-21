package com.example.ecommerce.repository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chạy một Call của Retrofit và đổi kết quả sang RepositoryCallback với thông báo lỗi tiếng Việt.
 */
abstract class BaseRepository {

    // Thông báo lỗi dùng chuỗi cố định để repository không phụ thuộc Context.
    private static final String ERROR_NETWORK = "Không kết nối được máy chủ. Kiểm tra backend và mạng rồi thử lại.";
    private static final String ERROR_UNKNOWN = "Đã có lỗi xảy ra. Thử lại sau.";

    protected <T> Call<T> enqueue(Call<T> call, RepositoryCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> c, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 404) {
                    callback.onError("Không tìm thấy dữ liệu.");
                } else {
                    callback.onError("Lỗi máy chủ (" + response.code() + "). Thử lại sau.");
                }
            }

            @Override
            public void onFailure(Call<T> c, Throwable t) {
                if (c.isCanceled()) {
                    return;
                }
                callback.onError(t instanceof IOException ? ERROR_NETWORK : ERROR_UNKNOWN);
            }
        });
        return call;
    }
}
