package com.example.ecommerce.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.BuildConfig;
import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentSettingsBinding;
import com.example.ecommerce.model.User;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.SessionManager;

/** Màn Cài đặt: xem thông tin tài khoản, phiên bản app và đăng xuất. */
public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        User user = SessionManager.getUser();
        if (user != null) {
            addInfoRow("Email", user.email);
            addInfoRow("Số điện thoại", user.phoneNumber);
        }
        addInfoRow("Giao diện", "Sáng");
        addInfoRow("Phiên bản", BuildConfig.VERSION_NAME);

        binding.logoutButton.setOnClickListener(v -> confirmLogout());
    }

    /** Thêm một dòng "nhãn ... giá trị" vào khung thông tin. */
    private void addInfoRow(String label, @Nullable String value) {
        View row = getLayoutInflater().inflate(R.layout.item_detail_row, binding.infoContainer, false);
        ((TextView) row.findViewById(R.id.row_label)).setText(label);
        ((TextView) row.findViewById(R.id.row_value)).setText(value == null ? "—" : value);
        binding.infoContainer.addView(row);
    }

    private void confirmLogout() {
        ConfirmDialog.show(requireContext(), "Đăng xuất?",
                "Bạn sẽ cần đăng nhập lại để xem giỏ hàng và đơn hàng.", "Đăng xuất", true, () -> {
                    SessionManager.logout();   // xóa token và thông tin người dùng
                    CheckoutSession.clear();   // xóa dữ liệu mua hàng đang dở
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.popBackStack(R.id.homeFragment, false); // về Trang chủ
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
