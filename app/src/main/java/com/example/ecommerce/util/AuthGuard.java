package com.example.ecommerce.util;

import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;

/**
 * Kiểm soát quyền truy cập yêu cầu trạng thái đăng nhập tài khoản.
 */
public final class AuthGuard {

    private AuthGuard() {
    }

    /** Kiểm tra xem phiên đăng nhập có hiệu lực hay không. Nếu chưa, chuyển tới màn Đăng nhập. */
    public static boolean requireLogin(Fragment fragment) {
        if (SessionManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(fragment.requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(fragment).navigate(R.id.loginFragment);
        return false;
    }
}
