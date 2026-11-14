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
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentRegisterBinding;
import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(
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

        binding.registerButton.setOnClickListener(v -> submit());

        binding.loginLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .popBackStack()
        );

        viewModel.getRegisterResult().observe(
                getViewLifecycleOwner(),
                this::render
        );
    }

    private void submit() {
        String email = binding.emailInput.getText()
                .toString().trim();

        String password = binding.passwordInput.getText()
                .toString();

        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);

        boolean valid = true;

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Nhập email hợp lệ");
            valid = false;
        }

        if (password.length() < 6) {
            binding.passwordLayout.setError(
                    "Mật khẩu tối thiểu 6 ký tự"
            );
            valid = false;
        }

        if (!valid) {
            return;
        }

        AuthModels.RegisterRequest request =
                new AuthModels.RegisterRequest();

        request.email = email;
        request.password = password;
        request.lastName = binding.lastNameInput.getText()
                .toString().trim();
        request.firstName = binding.firstNameInput.getText()
                .toString().trim();
        request.phoneNumber = binding.phoneInput.getText()
                .toString().trim();

        viewModel.register(request);
    }

    private void render(Resource<AuthModels.ApiMessage> result) {
        if (binding == null || result == null) {
            return;
        }

        switch (result.status) {
            case LOADING:
                binding.registerButton.setLoading(true);
                binding.errorText.setVisibility(View.GONE);
                break;

            case ERROR:
                binding.registerButton.setLoading(false);
                binding.errorText.setText(result.message);
                binding.errorText.setVisibility(View.VISIBLE);
                viewModel.clearRegisterResult();
                break;

            case SUCCESS:
                binding.registerButton.setLoading(false);
                binding.errorText.setVisibility(View.GONE);

                if (result.data != null
                        && result.data.message != null
                        && !result.data.message.trim().isEmpty()) {

                    Toast.makeText(
                            requireContext(),
                            result.data.message,
                            Toast.LENGTH_LONG
                    ).show();
                }

                String email = binding.emailInput.getText()
                        .toString().trim();

                viewModel.clearRegisterResult();

                Bundle args = new Bundle();
                args.putString(Constants.ARG_EMAIL, email);

                NavHostFragment.findNavController(this)
                        .navigate(R.id.verifyFragment, args);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}