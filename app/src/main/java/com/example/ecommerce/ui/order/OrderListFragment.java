package com.example.ecommerce.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentOrderListBinding;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;
import com.example.ecommerce.viewmodel.OrderViewModel;

import java.util.ArrayList;
import java.util.List;

/** Tab Đơn hàng: danh sách đơn của tôi, lọc theo trạng thái bằng các tab ngang. */
public class OrderListFragment extends Fragment {

    // Tên hiển thị và mã trạng thái tương ứng của từng tab (mã null = tất cả)
    private static final String[] TAB_NAMES = {"Tất cả", "Chờ xử lý", "Đang giao", "Chờ nhận", "Hoàn thành", "Đã hủy", "Hoàn trả"};
    private static final String[] TAB_STATUS = {null, "PENDING", "SHIPPING", "WAIT_DELIVER", "PAID", "CANCELED", "REFUND"};

    private FragmentOrderListBinding binding;
    private OrderViewModel viewModel;
    private OrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private int selectedTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrderListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        adapter = new OrderAdapter(this::openDetail);
        binding.orderList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.orderList.setAdapter(adapter);

        buildTabs();
        binding.loginPrompt.setMessage("Đăng nhập để xem đơn hàng của bạn");
        binding.loginPrompt.setOnLoginClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment));

        viewModel.getOrders().observe(getViewLifecycleOwner(), this::render);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SessionManager.isLoggedIn()) {
            binding.loginPrompt.setVisibility(View.GONE);
            viewModel.loadOrders();
        } else {
            binding.orderList.setVisibility(View.GONE);
            binding.stateView.hide();
            binding.loginPrompt.setVisibility(View.VISIBLE);
        }
    }

    /** Tạo các tab trạng thái bằng cách thêm từng chữ vào hàng ngang. */
    private void buildTabs() {
        binding.tabsContainer.removeAllViews();
        for (int i = 0; i < TAB_NAMES.length; i++) {
            final int index = i;
            TextView tab = (TextView) getLayoutInflater()
                    .inflate(R.layout.item_category_chip, binding.tabsContainer, false);
            tab.setText(TAB_NAMES[i]);
            tab.setSelected(i == selectedTab);
            tab.setOnClickListener(v -> {
                selectedTab = index;
                buildTabs();
                showOrders();
            });
            binding.tabsContainer.addView(tab);
        }
    }

    private void render(Resource<List<Order>> result) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        switch (result.status) {
            case LOADING:
                if (adapter.getItemCount() == 0) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                allOrders = (result.data == null) ? new ArrayList<Order>() : result.data;
                showOrders();
                break;
            case ERROR:
                binding.orderList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.loadOrders());
                break;
        }
    }

    /** Lọc danh sách đơn theo tab đang chọn rồi hiển thị. */
    private void showOrders() {
        String status = TAB_STATUS[selectedTab];
        List<Order> shown = new ArrayList<>();
        for (Order order : allOrders) {
            if (status == null || status.equals(order.status)) {
                shown.add(order);
            }
        }
        if (shown.isEmpty()) {
            binding.orderList.setVisibility(View.GONE);
            binding.stateView.showEmpty("Chưa có đơn hàng nào.", R.drawable.ic_orders);
            return;
        }
        binding.stateView.hide();
        binding.orderList.setVisibility(View.VISIBLE);
        adapter.submit(shown);
    }

    private void openDetail(Order order) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_ORDER_ID, order.id);
        NavHostFragment.findNavController(this).navigate(R.id.orderDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
