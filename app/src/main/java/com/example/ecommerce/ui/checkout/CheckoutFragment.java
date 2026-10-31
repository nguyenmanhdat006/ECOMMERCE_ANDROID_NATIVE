package com.example.ecommerce.ui.checkout;

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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentCheckoutBinding;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.ui.common.LineItemView;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.CheckoutViewModel;

import java.util.List;

/**
 * Màn Thanh toán (chỉ COD): xem lại địa chỉ, sản phẩm, tổng tiền rồi bấm Đặt hàng.
 * Danh sách sản phẩm lấy từ CheckoutSession (do Giỏ hàng hoặc nút "Mua ngay" điền sẵn).
 */
public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private CheckoutViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);

        binding.addressCard.setOnClickListener(v -> openAddressList());
        binding.changeAddress.setOnClickListener(v -> openAddressList());
        binding.addressEmpty.setOnClickListener(v -> openAddressList());

        binding.bottomBar.setLabel("Tổng thanh toán");
        binding.bottomBar.setPrimaryButton("Đặt hàng", v -> placeOrder());

        showItems();
        viewModel.getOrderResult().observe(getViewLifecycleOwner(), this::renderOrderResult);
    }

    /** Quay về từ màn chọn địa chỉ: hiển thị lại địa chỉ đang chọn. */
    @Override
    public void onResume() {
        super.onResume();
        if (CheckoutSession.getItems().isEmpty()) {
            // Không còn sản phẩm nào để thanh toán (ví dụ app vừa khởi động lại)
            Toast.makeText(requireContext(), "Không có sản phẩm để thanh toán.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        viewModel.selectDefaultAddress(this::showAddress);
    }

    /** Hiển thị danh sách sản phẩm và tổng tiền. */
    private void showItems() {
        List<CheckoutSession.Item> items = CheckoutSession.getItems();
        binding.itemsTitle.setText("Sản phẩm (" + items.size() + ")");
        binding.itemsContainer.removeAllViews();

        for (int i = 0; i < items.size(); i++) {
            CheckoutSession.Item item = items.get(i);
            LineItemView line = new LineItemView(requireContext());
            // Dòng phân loại kèm số lượng, ví dụ "M, Trắng × 2"
            line.bind(item.imageUrl, item.productName,
                    item.variantText + " × " + item.quantity,
                    Formatters.vnd(item.price * item.quantity));
            if (i > 0) {
                line.setPadding(0, dp(12), 0, 0);
            }
            binding.itemsContainer.addView(line);
        }

        String total = Formatters.vnd(CheckoutSession.getTotal());
        binding.subtotalText.setText(total);
        binding.totalText.setText(total);
        binding.bottomBar.setPrice(total);
    }

    /** Hiển thị địa chỉ đang chọn; nếu chưa có thì hiện lời mời thêm địa chỉ. */
    private void showAddress() {
        if (binding == null) {
            return;
        }
        Address address = CheckoutSession.getSelectedAddress();
        if (address == null) {
            binding.addressCard.setVisibility(View.GONE);
            binding.changeAddress.setVisibility(View.GONE);
            binding.addressEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.addressCard.bind(address.getDisplayName(), address.phoneNumber, address.getFullLine(), false);
            binding.addressCard.setVisibility(View.VISIBLE);
            binding.changeAddress.setVisibility(View.VISIBLE);
            binding.addressEmpty.setVisibility(View.GONE);
        }
    }

    private void openAddressList() {
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARG_SELECT_MODE, true); // chế độ chọn: bấm một địa chỉ là chọn
        NavHostFragment.findNavController(this).navigate(R.id.addressListFragment, args);
    }

    private void placeOrder() {
        Address address = CheckoutSession.getSelectedAddress();
        if (address == null) {
            Toast.makeText(requireContext(), "Hãy chọn địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        String notes = binding.notesInput.getText().toString().trim();
        viewModel.placeOrder(address, notes);
    }

    private void renderOrderResult(Resource<Order> result) {
        if (result == null) {
            return;
        }
        NavController navController = NavHostFragment.findNavController(this);
        switch (result.status) {
            case LOADING:
                binding.bottomBar.setPrimaryButton("Đang đặt hàng…", null);
                binding.bottomBar.setPrimaryEnabled(false);
                break;
            case SUCCESS:
                viewModel.clearOrderResult();
                Bundle success = new Bundle();
                success.putBoolean(Constants.ARG_SUCCESS, true);
                success.putString(Constants.ARG_ORDER_NUMBER, result.data.orderNumber);
                success.putDouble(Constants.ARG_ORDER_TOTAL, result.data.totalAmount);
                CheckoutSession.clear();
                // popUpTo: bỏ màn Thanh toán khỏi lịch sử để nút Back không quay lại đặt hàng lần nữa
                NavOptions options = new NavOptions.Builder().setPopUpTo(R.id.checkoutFragment, true).build();
                navController.navigate(R.id.orderResultFragment, success, options);
                break;
            case ERROR:
                viewModel.clearOrderResult();
                binding.bottomBar.setPrimaryButton("Đặt hàng", v -> placeOrder());
                binding.bottomBar.setPrimaryEnabled(true);
                Bundle failure = new Bundle();
                failure.putBoolean(Constants.ARG_SUCCESS, false);
                failure.putString(Constants.ARG_MESSAGE, result.message);
                navController.navigate(R.id.orderResultFragment, failure);
                break;
        }
    }

    /** Đổi dp sang pixel. */
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
