package com.example.ecommerce.ui.order;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentOrderDetailBinding;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.ui.common.LineItemView;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.OrderViewModel;

/**
 * Chi tiết một đơn hàng. Đơn đã hoàn thành (PAID) có nút "Đánh giá" cho từng sản phẩm chưa được đánh giá.
 * (Backend hiện chỉ cho Admin đổi trạng thái, nên khách chưa có nút hủy đơn.)
 */
public class OrderDetailFragment extends Fragment {

    private FragmentOrderDetailBinding binding;
    private OrderViewModel viewModel;
    private String orderId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);
        orderId = requireArguments().getString(Constants.ARG_ORDER_ID);
        viewModel.getOrder().observe(getViewLifecycleOwner(), this::render);
    }

    /** Tải lại mỗi khi quay về (ví dụ sau khi vừa đánh giá xong thì nút Đánh giá phải biến mất). */
    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadOrder(orderId);
    }

    private void render(Resource<Order> result) {
        switch (result.status) {
            case LOADING:
                if (binding.scroll.getVisibility() != View.VISIBLE) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                binding.stateView.hide();
                binding.scroll.setVisibility(View.VISIBLE);
                showOrder(result.data);
                break;
            case ERROR:
                binding.scroll.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.loadOrder(orderId));
                break;
        }
    }

    private void showOrder(Order order) {
        binding.orderNumber.setText(order.orderNumber);
        binding.orderDate.setText("Đặt ngày " + Formatters.date(order.orderDate));
        binding.orderStatus.setStatus(order.status);
        binding.shippingAddress.setText(order.shippingAddress);
        binding.totalText.setText(Formatters.vnd(order.totalAmount));

        String paymentName = "COD".equals(order.paymentMethod) ? "Thanh toán khi nhận hàng" : order.paymentMethod;
        binding.paymentText.setText(paymentName);

        boolean hasNotes = order.notes != null && !order.notes.isEmpty();
        binding.notesRow.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
        binding.notesText.setText(order.notes);

        showItems(order);
    }

    /** Liệt kê các sản phẩm của đơn; đơn đã hoàn thành thì thêm nút Đánh giá dưới sản phẩm chưa đánh giá. */
    private void showItems(Order order) {
        binding.itemsContainer.removeAllViews();
        boolean canReview = "PAID".equals(order.status);

        for (int i = 0; i < order.orderItems.size(); i++) {
            final Order.OrderItem item = order.orderItems.get(i);

            LineItemView line = new LineItemView(requireContext());
            line.bind(item.thumbnail, item.productName, "Số lượng: " + item.quantity,
                    Formatters.vnd(item.totalPrice));
            if (i > 0) {
                line.setPadding(0, dp(12), 0, 0);
            }
            binding.itemsContainer.addView(line);

            if (canReview) {
                binding.itemsContainer.addView(buildReviewRow(item));
            }
        }
    }

    /** Một hàng bên dưới sản phẩm: nút "Đánh giá" hoặc chữ "Đã đánh giá". */
    private View buildReviewRow(final Order.OrderItem item) {
        TextView action = new TextView(requireContext());
        action.setPadding(dp(12), dp(6), dp(12), dp(6));
        action.setTextSize(13);
        if (item.reviewed) {
            action.setText("Đã đánh giá");
            action.setTextColor(requireContext().getColor(R.color.text_secondary));
        } else {
            action.setText("Đánh giá");
            action.setTextColor(requireContext().getColor(R.color.primary));
            action.setOnClickListener(v -> openWriteReview(item));
        }

        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(Gravity.END);
        row.addView(action);
        return row;
    }

    private void openWriteReview(Order.OrderItem item) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, item.productId);
        args.putString(Constants.ARG_ORDER_ITEM_ID, item.id);
        args.putString(Constants.ARG_PRODUCT_NAME, item.productName);
        args.putString(Constants.ARG_IMAGE_URL, item.thumbnail);
        NavHostFragment.findNavController(this).navigate(R.id.writeReviewFragment, args);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
