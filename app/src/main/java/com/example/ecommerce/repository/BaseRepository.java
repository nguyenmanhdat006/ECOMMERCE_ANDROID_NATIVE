package com.example.ecommerce.repository;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Quản lý nền tảng thực thi các yêu cầu API HTTP với Retrofit và xử lý phản hồi Callback.
 */
abstract class BaseRepository {

    private static final String ERROR_NETWORK = "Không kết nối được máy chủ. Kiểm tra backend và mạng rồi thử lại.";
    private static final String ERROR_UNKNOWN = "Đã có lỗi xảy ra. Thử lại sau.";
    private static final String ERROR_SESSION = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";

    /** Đưa yêu cầu HTTP vào hàng đợi xử lý bất đồng bộ. */
    protected <T> Call<T> enqueue(Call<T> call, RepositoryCallback<T> callback) {
        return enqueue(call, callback, ERROR_SESSION);
    }

    /** Thực thi yêu cầu bất đồng bộ với tùy chỉnh thông điệp cho mã lỗi 401. */
    protected <T> Call<T> enqueue(Call<T> call, RepositoryCallback<T> callback, String message401) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> c, Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(messageForError(response, message401));
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

    /** Chuyển đổi mã phản hồi HTTP sang thông báo giao diện. */
    private String messageForError(Response<?> response, String message401) {
        int statusCode = response.code();
        if (statusCode == 401) {
            return message401;
        }
        if (statusCode == 403) {
            return "Bạn không có quyền thực hiện thao tác này.";
        }
        if (statusCode == 404) {
            return "Không tìm thấy dữ liệu.";
        }
        if (statusCode == 400 || statusCode == 409) {
            String errMsg = readServerMessage(response);
            return errMsg != null ? errMsg : "Dữ liệu không hợp lệ.";
        }
        return "Lỗi máy chủ (" + statusCode + "). Thử lại sau.";
    }

    /** Đọc thông điệp chi tiết từ phía backend trả về. */
    private String readServerMessage(Response<?> response) {
        try {
            String rawBody = response.errorBody().string();
            JsonObject jsonObj = JsonParser.parseString(rawBody).getAsJsonObject();
            if (jsonObj.has("message")) {
                return jsonObj.get("message").getAsString();
            }
        } catch (Exception e) {
            // Sử dụng thông báo chuẩn khi không giải mã được nội dung
        }
        return null;
    }
}
