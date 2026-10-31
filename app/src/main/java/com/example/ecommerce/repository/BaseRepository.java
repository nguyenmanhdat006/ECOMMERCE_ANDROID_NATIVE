package com.example.ecommerce.repository;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lớp cha của các repository: chạy một yêu cầu Retrofit và trả kết quả về RepositoryCallback.
 * Lỗi được đổi sang câu tiếng Việt dễ hiểu để màn hình hiển thị luôn.
 */
abstract class BaseRepository {

    private static final String ERROR_NETWORK = "Không kết nối được máy chủ. Kiểm tra backend và mạng rồi thử lại.";
    private static final String ERROR_UNKNOWN = "Đã có lỗi xảy ra. Thử lại sau.";
    private static final String ERROR_SESSION = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";

    /** Gửi yêu cầu. Thất bại 401 sẽ báo "phiên đăng nhập hết hạn". */
    protected <T> Call<T> enqueue(Call<T> call, RepositoryCallback<T> callback) {
        return enqueue(call, callback, ERROR_SESSION);
    }

    /**
     * Gửi yêu cầu, cho phép đặt câu báo lỗi riêng cho mã 401
     * (ví dụ màn Đăng nhập: 401 nghĩa là "sai email hoặc mật khẩu").
     */
    protected <T> Call<T> enqueue(Call<T> call, RepositoryCallback<T> callback, String message401) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> c, Response<T> response) {
                if (response.isSuccessful()) {
                    // Với Call<Void> thì body() là null, vẫn là thành công
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(messageForError(response, message401));
                }
            }

            @Override
            public void onFailure(Call<T> c, Throwable t) {
                if (c.isCanceled()) {
                    return; // yêu cầu đã bị hủy chủ động, không báo lỗi
                }
                callback.onError(t instanceof IOException ? ERROR_NETWORK : ERROR_UNKNOWN);
            }
        });
        return call;
    }

    /** Chọn câu báo lỗi theo mã HTTP mà máy chủ trả về. */
    private String messageForError(Response<?> response, String message401) {
        int code = response.code();
        if (code == 401) {
            return message401;
        }
        if (code == 403) {
            return "Bạn không có quyền thực hiện thao tác này.";
        }
        if (code == 404) {
            return "Không tìm thấy dữ liệu.";
        }
        if (code == 400 || code == 409) {
            String serverMessage = readServerMessage(response);
            return serverMessage != null ? serverMessage : "Dữ liệu không hợp lệ.";
        }
        return "Lỗi máy chủ (" + code + "). Thử lại sau.";
    }

    /** Backend báo lỗi dạng { "message": "..." }; lấy câu đó ra nếu có. */
    private String readServerMessage(Response<?> response) {
        try {
            String body = response.errorBody().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            if (json.has("message")) {
                return json.get("message").getAsString();
            }
        } catch (Exception e) {
            // Không đọc được nội dung lỗi thì dùng câu mặc định
        }
        return null;
    }
}
