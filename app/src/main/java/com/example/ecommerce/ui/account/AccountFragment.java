package com.example.ecommerce.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentAccountBinding;
import com.example.ecommerce.model.User;
import com.example.ecommerce.util.SessionManager;

import java.util.Locale;

/** Tab Tài khoản: thông tin người dùng và lối vào Đơn hàng, Chờ đánh giá, Địa chỉ, Cài đặt. */
public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final NavController navController = NavHostFragment.findNavController(this);

        binding.menuOrders.setOnClickListener(v -> navController.navigate(R.id.ordersFragment));
        binding.menuUnreviewed.setOnClickListener(v -> navController.navigate(R.id.unreviewedFragment));
        binding.menuAddresses.setOnClickListener(v -> navController.navigate(R.id.addressListFragment));
        binding.menuSettings.setOnClickListener(v -> navController.navigate(R.id.settingsFragment));

        binding.loginPrompt.setMessage("Đăng nhập để xem đơn hàng và địa chỉ của bạn");
        binding.loginPrompt.setOnLoginClickListener(v -> navController.navigate(R.id.loginFragment));
    }

    /** Mỗi lần mở tab (và sau khi đăng nhập hoặc đăng xuất) hiển thị lại đúng trạng thái. */
    @Override
    public void onResume() {
        super.onResume();
        User user = SessionManager.getUser();
        if (user == null) {
            binding.content.setVisibility(View.GONE);
            binding.loginPrompt.setVisibility(View.VISIBLE);
            return;
        }
        binding.loginPrompt.setVisibility(View.GONE);
        binding.content.setVisibility(View.VISIBLE);

        String name = user.getFullName();
        binding.userName.setText(name);
        binding.userEmail.setText(user.email);
        binding.userPhone.setText(user.phoneNumber == null ? "" : user.phoneNumber);
        // Ảnh đại diện tạm: chữ cái đầu của tên
        binding.avatarText.setText(name.substring(0, 1).toUpperCase(Locale.getDefault()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
