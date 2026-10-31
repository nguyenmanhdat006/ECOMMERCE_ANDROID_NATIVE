package com.example.ecommerce.repository;

import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

/** Đăng nhập, đăng ký và xác thực email. */
public class AuthRepository extends BaseRepository {

    private static final String WRONG_LOGIN = "Email hoặc mật khẩu không đúng.";

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public void login(String email, String password, RepositoryCallback<AuthModels.LoginResponse> callback) {
        if (Constants.USE_MOCK_DATA) {
            AuthModels.LoginResponse response = MockStore.login(email, password);
            if (response == null) {
                MockDataSource.error(callback, WRONG_LOGIN);
            } else {
                MockDataSource.success(callback, response);
            }
            return;
        }
        // Với API đăng nhập, mã 401 nghĩa là sai email hoặc mật khẩu
        enqueue(api.login(new AuthModels.LoginRequest(email, password)), callback, WRONG_LOGIN);
    }

    public void register(AuthModels.RegisterRequest request, RepositoryCallback<AuthModels.ApiMessage> callback) {
        if (Constants.USE_MOCK_DATA) {
            String error = MockStore.register(request);
            if (error != null) {
                MockDataSource.error(callback, error);
            } else {
                AuthModels.ApiMessage message = new AuthModels.ApiMessage();
                message.code = 200;
                message.message = "User created!";
                MockDataSource.success(callback, message);
            }
            return;
        }
        enqueue(api.register(request), callback);
    }

    public void verify(String email, String code, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            if (MockStore.verify(email, code)) {
                MockDataSource.success(callback, null);
            } else {
                MockDataSource.error(callback, "Mã không đúng. Kiểm tra lại và thử lần nữa.");
            }
            return;
        }
        enqueue(api.verify(new AuthModels.VerifyRequest(email, code)), callback, "Mã không đúng. Kiểm tra lại và thử lần nữa.");
    }
}
