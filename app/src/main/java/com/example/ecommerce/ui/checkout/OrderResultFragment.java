package com.example.ecommerce.ui.checkout;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentOrderResultBinding;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Formatters;

/**
 * Kết quả đặt hàng.
 *  - Thành công: hiện mã đơn, tổng tiền; nút "Xem đơn hàng" và "Tiếp tục mua sắm".
 *  - Thất bại (ví dụ hết hàng): hiện lý do; nút "Quay lại thanh toán" và "Về giỏ hàng".
 */
public class OrderResultFragment extends Fragment {

    private FragmentOrderResultBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        NavController navController = NavHostFragment.findNavController(this);

        if (args.getBoolean(Constants.ARG_SUCCESS, false)) {
            showSuccess(args, navController);
        } else {
            showFailure(args.getString(Constants.ARG_MESSAGE), navController);
        }
    }

    private void showSuccess(Bundle args, final NavController navController) {
        setIcon(R.drawable.ic_check_circle, R.color.success);
        binding.resultTitle.setText("Đặt hàng thành công");
        binding.resultMessage.setText("Mã đơn " + args.getString(Constants.ARG_ORDER_NUMBER));
        binding.summaryCard.setVisibility(View.VISIBLE);
        binding.summaryTotal.setText(Formatters.vnd(args.getDouble(Constants.ARG_ORDER_TOTAL)));
        binding.summaryStatus.setStatus("PENDING");

        binding.primaryButton.setText("Xem đơn hàng");
        binding.primaryButton.setOnClickListener(v -> {
            navController.popBackStack(R.id.homeFragment, false); // về Trang chủ rồi mở tab Đơn hàng
            navController.navigate(R.id.ordersFragment);
        });
        binding.secondaryButton.setText("Tiếp tục mua sắm");
        binding.secondaryButton.setOnClickListener(v -> navController.popBackStack(R.id.homeFragment, false));

        // Nút Back của hệ thống cũng đưa về Trang chủ (không quay lại màn đặt hàng)
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navController.popBackStack(R.id.homeFragment, false);
                    }
                });
    }

    private void showFailure(String message, final NavController navController) {
        setIcon(R.drawable.ic_error, R.color.danger);
        binding.resultTitle.setText("Không thể đặt hàng");
        binding.resultMessage.setText(message);
        binding.summaryCard.setVisibility(View.GONE);

        binding.primaryButton.setText("Quay lại thanh toán");
        binding.primaryButton.setOnClickListener(v -> navController.popBackStack());
        binding.secondaryButton.setText("Về giỏ hàng");
        binding.secondaryButton.setOnClickListener(v -> {
            navController.popBackStack(R.id.homeFragment, false);
            navController.navigate(R.id.cartFragment);
        });
    }

    private void setIcon(int iconRes, int colorRes) {
        binding.resultIcon.setImageResource(iconRes);
        int color = ContextCompat.getColor(requireContext(), colorRes);
        ImageViewCompat.setImageTintList(binding.resultIcon, ColorStateList.valueOf(color));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
