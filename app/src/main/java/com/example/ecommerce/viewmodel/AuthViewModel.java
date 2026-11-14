package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.repository.AuthRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;

<<<<<<< HEAD
/**
 * Quản lý logic và trạng thái các luồng Đăng nhập, Đăng ký và Xác thực tài khoản.
 */
=======
>>>>>>> 20ba9a7cedd951ceef69aca31d50c2a189689380
public class AuthViewModel extends ViewModel {

    private final AuthRepository repository = new AuthRepository();

    private final MutableLiveData<Resource<AuthModels.LoginResponse>>
            loginResult = new MutableLiveData<>();

    private final MutableLiveData<Resource<AuthModels.ApiMessage>>
            registerResult = new MutableLiveData<>();

    private final MutableLiveData<Resource<Void>>
            verifyResult = new MutableLiveData<>();

    private final MutableLiveData<Resource<AuthModels.ApiMessage>>
            resendResult = new MutableLiveData<>();

    private boolean resendInProgress = false;

    public LiveData<Resource<AuthModels.LoginResponse>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Resource<AuthModels.ApiMessage>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Resource<Void>> getVerifyResult() {
        return verifyResult;
    }

    public LiveData<Resource<AuthModels.ApiMessage>> getResendResult() {
        return resendResult;
    }

    public void login(String email, String password) {
<<<<<<< HEAD
        loginResult.setValue(Resource.<AuthModels.LoginResponse>loading());
        repository.login(email, password, new RepositoryCallback<AuthModels.LoginResponse>() {
            @Override
            public void onSuccess(AuthModels.LoginResponse data) {
                SessionManager.saveLogin(data.token, data.user);
                loginResult.setValue(Resource.success(data));
            }
=======
        loginResult.setValue(
                Resource.<AuthModels.LoginResponse>loading()
        );
>>>>>>> 20ba9a7cedd951ceef69aca31d50c2a189689380

        repository.login(
                email,
                password,
                new RepositoryCallback<AuthModels.LoginResponse>() {
                    @Override
                    public void onSuccess(AuthModels.LoginResponse data) {
                        if (data == null
                                || data.token == null
                                || data.token.trim().isEmpty()
                                || data.user == null) {
                            loginResult.setValue(
                                    Resource.<AuthModels.LoginResponse>error(
                                            "Phản hồi đăng nhập không hợp lệ."
                                    )
                            );
                            return;
                        }

                        SessionManager.saveLogin(data.token, data.user);
                        loginResult.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        loginResult.setValue(
                                Resource.<AuthModels.LoginResponse>error(message)
                        );
                    }
                }
        );
    }

    public void register(AuthModels.RegisterRequest request) {
        registerResult.setValue(
                Resource.<AuthModels.ApiMessage>loading()
        );

        repository.register(
                request,
                new RepositoryCallback<AuthModels.ApiMessage>() {
                    @Override
                    public void onSuccess(AuthModels.ApiMessage data) {
                        registerResult.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        registerResult.setValue(
                                Resource.<AuthModels.ApiMessage>error(message)
                        );
                    }
                }
        );
    }

    public void verify(String email, String code) {
        verifyResult.setValue(Resource.<Void>loading());

        repository.verify(
                email,
                code,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        verifyResult.setValue(Resource.<Void>success(null));
                    }

                    @Override
                    public void onError(String message) {
                        verifyResult.setValue(Resource.<Void>error(message));
                    }
                }
        );
    }

    public void resendVerification(String email) {
        if (resendInProgress) {
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            resendResult.setValue(
                    Resource.<AuthModels.ApiMessage>error(
                            "Không tìm thấy email cần xác thực."
                    )
            );
            return;
        }

        resendInProgress = true;
        resendResult.setValue(
                Resource.<AuthModels.ApiMessage>loading()
        );

        repository.resendVerification(
                email.trim(),
                new RepositoryCallback<AuthModels.ApiMessage>() {
                    @Override
                    public void onSuccess(AuthModels.ApiMessage data) {
                        resendInProgress = false;
                        resendResult.setValue(Resource.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        resendInProgress = false;
                        resendResult.setValue(
                                Resource.<AuthModels.ApiMessage>error(message)
                        );
                    }
                }
        );
    }

    public void clearLoginResult() {
        loginResult.setValue(null);
    }

    public void clearRegisterResult() {
        registerResult.setValue(null);
    }

    public void clearVerifyResult() {
        verifyResult.setValue(null);
    }

    public void clearResendResult() {
        resendResult.setValue(null);
    }
}