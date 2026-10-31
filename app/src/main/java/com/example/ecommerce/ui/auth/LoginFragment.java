package com.example.ecommerce.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentLoginBinding;
import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AuthViewModel;

/** Màn hình Đăng nhập. Đăng nhập xong thì quay lại màn hình trước đó. */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.loginButton.setOnClickListener(v -> submit());
        binding.registerLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.registerFragment));

        // Nhắc tài khoản thử khi đang dùng dữ liệu giả
        if (Constants.USE_MOCK_DATA) {
            binding.mockHint.setVisibility(View.VISIBLE);
            binding.mockHint.setText("Đang dùng dữ liệu giả. Tài khoản thử:\nlan@example.com / Test@12345");
        }

        viewModel.getLoginResult().observe(getViewLifecycleOwner(), this::render);
    }

    /** Kiểm tra dữ liệu nhập rồi gọi đăng nhập. */
    private void submit() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();

        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        boolean valid = true;
        if (!email.contains("@")) {
            binding.emailLayout.setError("Nhập email hợp lệ");
            valid = false;
        }
        if (password.isEmpty()) {
            binding.passwordLayout.setError("Nhập mật khẩu");
            valid = false;
        }
        if (valid) {
            viewModel.login(email, password);
        }
    }

    private void render(Resource<AuthModels.LoginResponse> result) {
        if (result == null) {
            return;
        }
        switch (result.status) {
            case LOADING:
                binding.loginButton.setLoading(true);
                binding.errorText.setVisibility(View.GONE);
                break;
            case ERROR:
                binding.loginButton.setLoading(false);
                binding.errorText.setText(result.message);
                binding.errorText.setVisibility(View.VISIBLE);
                viewModel.clearLoginResult();
                break;
            case SUCCESS:
                binding.loginButton.setLoading(false);
                viewModel.clearLoginResult();
                // Đăng nhập xong: quay lại màn hình đã mở ra màn Đăng nhập
                NavHostFragment.findNavController(this).popBackStack();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
