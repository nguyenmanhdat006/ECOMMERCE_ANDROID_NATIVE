package com.example.ecommerce.util;

import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;

/**
 * Kiểm tra đăng nhập trước khi làm việc cần tài khoản (thêm giỏ hàng, mua hàng...).
 *
 * Cách dùng trong Fragment:
 *   if (!AuthGuard.requireLogin(this)) return;   // chưa đăng nhập thì tự mở màn Đăng nhập
 */
public final class AuthGuard {

    private AuthGuard() {
    }

    /** Trả về true nếu đã đăng nhập. Nếu chưa: báo, mở màn Đăng nhập và trả về false. */
    public static boolean requireLogin(Fragment fragment) {
        if (SessionManager.isLoggedIn()) {
            return true;
        }
        Toast.makeText(fragment.requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
        NavHostFragment.findNavController(fragment).navigate(R.id.loginFragment);
        return false;
    }
}
