package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.repository.AuthRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;

/**
 * Xử lý đăng nhập, đăng ký, xác thực email.
 *
 * Mỗi thao tác có một LiveData kết quả. Sau khi màn hình xử lý xong kết quả (ví dụ chuyển trang),
 * hãy gọi clear...() để kết quả cũ không bị xử lý lại khi quay về màn hình này.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Resource<AuthModels.LoginResponse>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<AuthModels.ApiMessage>> registerResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> verifyResult = new MutableLiveData<>();

    public LiveData<Resource<AuthModels.LoginResponse>> getLoginResult() { return loginResult; }

    public LiveData<Resource<AuthModels.ApiMessage>> getRegisterResult() { return registerResult; }

    public LiveData<Resource<Void>> getVerifyResult() { return verifyResult; }

    public void login(String email, String password) {
        loginResult.setValue(Resource.<AuthModels.LoginResponse>loading());
        repository.login(email, password, new RepositoryCallback<AuthModels.LoginResponse>() {
            @Override
            public void onSuccess(AuthModels.LoginResponse data) {
                // Lưu token để các lần gọi API sau tự gắn vào header
                SessionManager.saveLogin(data.token, data.user);
                loginResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                loginResult.setValue(Resource.<AuthModels.LoginResponse>error(message));
            }
        });
    }

    public void register(AuthModels.RegisterRequest request) {
        registerResult.setValue(Resource.<AuthModels.ApiMessage>loading());
        repository.register(request, new RepositoryCallback<AuthModels.ApiMessage>() {
            @Override
            public void onSuccess(AuthModels.ApiMessage data) {
                registerResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                registerResult.setValue(Resource.<AuthModels.ApiMessage>error(message));
            }
        });
    }

    public void verify(String email, String code) {
        verifyResult.setValue(Resource.<Void>loading());
        repository.verify(email, code, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                verifyResult.setValue(Resource.<Void>success(null));
            }

            @Override
            public void onError(String message) {
                verifyResult.setValue(Resource.<Void>error(message));
            }
        });
    }

    public void clearLoginResult() { loginResult.setValue(null); }

    public void clearRegisterResult() { registerResult.setValue(null); }

    public void clearVerifyResult() { verifyResult.setValue(null); }
}
