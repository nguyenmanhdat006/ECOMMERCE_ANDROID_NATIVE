package com.example.ecommerce.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentUnreviewedBinding;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.OrderViewModel;

import java.util.List;

/** Màn hình "Chờ đánh giá": các sản phẩm đã mua (đơn hoàn thành) mà chưa được đánh giá. */
public class UnreviewedFragment extends Fragment {

    private FragmentUnreviewedBinding binding;
    private OrderViewModel viewModel;
    private UnreviewedAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUnreviewedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OrderViewModel.class);

        adapter = new UnreviewedAdapter(this::openWriteReview);
        binding.itemList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.itemList.setAdapter(adapter);

        viewModel.getUnreviewed().observe(getViewLifecycleOwner(), this::render);
    }

    /** Tải lại mỗi khi quay về: sản phẩm vừa đánh giá xong phải biến mất khỏi danh sách. */
    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUnreviewed();
    }

    private void render(Resource<List<Order.OrderItem>> result) {
        switch (result.status) {
            case LOADING:
                if (adapter.getItemCount() == 0) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                if (result.data == null || result.data.isEmpty()) {
                    binding.itemList.setVisibility(View.GONE);
                    binding.stateView.showEmpty("Bạn đã đánh giá hết sản phẩm đã mua.", R.drawable.ic_star);
                } else {
                    binding.stateView.hide();
                    binding.itemList.setVisibility(View.VISIBLE);
                    adapter.submit(result.data);
                }
                break;
            case ERROR:
                binding.itemList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.loadUnreviewed());
                break;
        }
    }

    private void openWriteReview(Order.OrderItem item) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, item.productId);
        args.putString(Constants.ARG_ORDER_ITEM_ID, item.id);
        args.putString(Constants.ARG_PRODUCT_NAME, item.productName);
        args.putString(Constants.ARG_IMAGE_URL, item.thumbnail);
        NavHostFragment.findNavController(this).navigate(R.id.writeReviewFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
