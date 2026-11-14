package com.example.ecommerce.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentVerifyBinding;
import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AuthViewModel;

public class VerifyFragment extends Fragment {

    private FragmentVerifyBinding binding;
    private AuthViewModel viewModel;
    private String email;

    private boolean verifying = false;
    private boolean resending = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentVerifyBinding.inflate(
                inflater, container, false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this)
                .get(AuthViewModel.class);

        Bundle args = getArguments();
        email = args == null
                ? null
                : args.getString(Constants.ARG_EMAIL);

        if (email == null || email.trim().isEmpty()) {
            binding.verifyHint.setText("Không tìm thấy email cần xác thực.");
            binding.verifyButton.setEnabled(false);
            binding.resendButton.setEnabled(false);
            return;
        }

        email = email.trim();

        binding.verifyHint.setText(
                "Nhập mã 6 số trong email gửi tới\n" + email
        );

        binding.verifyButton.setOnClickListener(v -> submit());

        binding.resendButton.setOnClickListener(v -> {
            if (!verifying && !resending) {
                viewModel.resendVerification(email);
            }
        });

        if (Constants.USE_MOCK_DATA) {
            binding.mockHint.setVisibility(View.VISIBLE);
            binding.mockHint.setText(
                    "Đang dùng dữ liệu giả. Mã xác thực: 123456"
            );
        }

        viewModel.getVerifyResult().observe(
                getViewLifecycleOwner(),
                this::renderVerify
        );

        viewModel.getResendResult().observe(
                getViewLifecycleOwner(),
                this::renderResend
        );
    }

    private void submit() {
        if (verifying || resending) {
            return;
        }

        String code = binding.codeInput.getText()
                .toString().trim();

        if (!code.matches("[0-9]{6}")) {
            showError("Nhập đúng mã gồm 6 chữ số.");
            return;
        }

        viewModel.verify(email, code);
    }

    private void renderVerify(Resource<Void> result) {
        if (binding == null || result == null) {
            return;
        }

        switch (result.status) {
            case LOADING:
                verifying = true;
                binding.errorText.setVisibility(View.GONE);
                updateButtons();
                break;

            case ERROR:
                verifying = false;
                updateButtons();
                showError(result.message);
                viewModel.clearVerifyResult();
                break;

            case SUCCESS:
                verifying = false;
                updateButtons();
                viewModel.clearVerifyResult();

                Toast.makeText(
                        requireContext(),
                        "Đã kích hoạt tài khoản. Hãy đăng nhập.",
                        Toast.LENGTH_LONG
                ).show();

                NavController navController =
                        NavHostFragment.findNavController(this);

                if (!navController.popBackStack(
                        R.id.loginFragment, false)) {
                    navController.navigate(R.id.loginFragment);
                }
                break;
        }
    }

    private void renderResend(
            Resource<AuthModels.ApiMessage> result) {

        if (binding == null || result == null) {
            return;
        }

        switch (result.status) {
            case LOADING:
                resending = true;
                binding.errorText.setVisibility(View.GONE);
                updateButtons();
                break;

            case ERROR:
                resending = false;
                updateButtons();
                showError(result.message);
                viewModel.clearResendResult();
                break;

            case SUCCESS:
                resending = false;
                updateButtons();
                binding.errorText.setVisibility(View.GONE);

                // Mã cũ không còn dùng được sau khi gửi lại mã.
                binding.codeInput.setText("");

                String message = "Đã gửi mã mới. Kiểm tra email và Spam.";

                if (result.data != null
                        && result.data.message != null
                        && !result.data.message.trim().isEmpty()) {
                    message = result.data.message;
                }

                Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                ).show();

                viewModel.clearResendResult();
                break;
        }
    }

    private void updateButtons() {
        boolean busy = verifying || resending;

        binding.verifyButton.setLoading(verifying);
        binding.verifyButton.setEnabled(!busy);

        binding.resendButton.setEnabled(!busy);
        binding.resendButton.setText(
                resending ? "Đang gửi..." : "Gửi lại mã"
        );
    }

    private void showError(String message) {
        binding.errorText.setText(
                message == null || message.trim().isEmpty()
                        ? "Đã có lỗi xảy ra. Vui lòng thử lại."
                        : message
        );

        binding.errorText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        verifying = false;
        resending = false;
    }
}