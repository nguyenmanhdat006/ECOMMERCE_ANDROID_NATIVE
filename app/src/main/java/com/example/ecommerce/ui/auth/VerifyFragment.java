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
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AuthViewModel;

/** Màn hình nhập mã 6 số gửi qua email để kích hoạt tài khoản. */
public class VerifyFragment extends Fragment {

    private FragmentVerifyBinding binding;
    private AuthViewModel viewModel;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        email = requireArguments().getString(Constants.ARG_EMAIL);

        binding.verifyHint.setText("Nhập mã 6 số đã gửi tới\n" + email);
        binding.verifyButton.setOnClickListener(v -> submit());

        if (Constants.USE_MOCK_DATA) {
            binding.mockHint.setVisibility(View.VISIBLE);
            binding.mockHint.setText("Đang dùng dữ liệu giả. Mã xác thực: 123456");
        }

        viewModel.getVerifyResult().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String code = binding.codeInput.getText().toString().trim();
        if (code.length() != 6) {
            binding.errorText.setText("Nhập đủ 6 số.");
            binding.errorText.setVisibility(View.VISIBLE);
            return;
        }
        viewModel.verify(email, code);
    }

    private void render(Resource<Void> result) {
        if (result == null) {
            return;
        }
        switch (result.status) {
            case LOADING:
                binding.verifyButton.setLoading(true);
                binding.errorText.setVisibility(View.GONE);
                break;
            case ERROR:
                binding.verifyButton.setLoading(false);
                binding.errorText.setText(result.message);
                binding.errorText.setVisibility(View.VISIBLE);
                viewModel.clearVerifyResult();
                break;
            case SUCCESS:
                binding.verifyButton.setLoading(false);
                viewModel.clearVerifyResult();
                Toast.makeText(requireContext(), "Đã kích hoạt tài khoản. Hãy đăng nhập.", Toast.LENGTH_LONG).show();
                // Quay về màn Đăng nhập (bỏ qua màn Đăng ký và Xác thực khỏi lịch sử)
                NavController navController = NavHostFragment.findNavController(this);
                navController.popBackStack(R.id.loginFragment, false);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
