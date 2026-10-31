package com.example.ecommerce.ui.cart;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentCartBinding;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;
import com.example.ecommerce.viewmodel.CartViewModel;

import java.util.ArrayList;
import java.util.List;

/** Tab Giỏ hàng: xem, đổi số lượng, xóa sản phẩm và chuyển sang Thanh toán. */
public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartViewModel viewModel;
    private CartAdapter adapter;
    private List<CartItem> currentItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CartViewModel.class);

        adapter = new CartAdapter(new CartAdapter.OnCartActionListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                viewModel.changeQuantity(item, newQuantity);
            }

            @Override
            public void onRemove(CartItem item) {
                confirmRemove(item);
            }
        });
        binding.cartList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cartList.setAdapter(adapter);

        binding.loginPrompt.setMessage("Đăng nhập để xem giỏ hàng của bạn");
        binding.loginPrompt.setOnLoginClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment));

        binding.bottomBar.setLabel("Tổng cộng");
        binding.bottomBar.setPrimaryButton("Mua hàng", v -> goToCheckout());

        viewModel.getCart().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });
    }

    /** Mỗi lần mở tab: nếu đã đăng nhập thì tải giỏ hàng, chưa thì hiện lời nhắc đăng nhập. */
    @Override
    public void onResume() {
        super.onResume();
        if (SessionManager.isLoggedIn()) {
            binding.loginPrompt.setVisibility(View.GONE);
            viewModel.load();
        } else {
            showLoginPrompt();
        }
    }

    private void showLoginPrompt() {
        binding.cartList.setVisibility(View.GONE);
        binding.stateView.hide();
        binding.bottomBar.setVisibility(View.GONE);
        binding.loginPrompt.setVisibility(View.VISIBLE);
    }

    private void render(Resource<List<CartItem>> result) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        switch (result.status) {
            case LOADING:
                // Chỉ hiện vòng quay khi chưa có gì để xem
                if (adapter.getItemCount() == 0) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                showCart(result.data);
                break;
            case ERROR:
                binding.cartList.setVisibility(View.GONE);
                binding.bottomBar.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.load());
                break;
        }
    }

    private void showCart(List<CartItem> items) {
        currentItems = (items == null) ? new ArrayList<CartItem>() : items;
        if (currentItems.isEmpty()) {
            binding.cartList.setVisibility(View.GONE);
            binding.bottomBar.setVisibility(View.GONE);
            binding.stateView.showEmpty("Giỏ hàng đang trống.\nHãy thêm sản phẩm bạn thích.", R.drawable.ic_cart);
            return;
        }
        binding.stateView.hide();
        binding.cartList.setVisibility(View.VISIBLE);
        adapter.submit(currentItems);

        // Tổng tiền = cộng thành tiền của từng dòng
        double total = 0;
        for (CartItem item : currentItems) {
            total += item.getLineTotal();
        }
        binding.bottomBar.setPrice(Formatters.vnd(total));
        binding.bottomBar.setVisibility(View.VISIBLE);
    }

    private void confirmRemove(final CartItem item) {
        ConfirmDialog.show(requireContext(), "Xóa sản phẩm?", "Sản phẩm sẽ bị xóa khỏi giỏ hàng.",
                "Xóa", true, () -> viewModel.remove(item));
    }

    /** Chuyển các dòng trong giỏ sang dữ liệu mua hàng rồi mở màn Thanh toán. */
    private void goToCheckout() {
        List<CheckoutSession.Item> items = new ArrayList<>();
        for (CartItem cartItem : currentItems) {
            CheckoutSession.Item item = new CheckoutSession.Item();
            item.variantId = cartItem.productVariant.getId();
            item.productName = cartItem.product.getName();
            item.imageUrl = cartItem.product.getCoverUrl();
            item.variantText = cartItem.getVariantText();
            item.price = cartItem.product.getPrice();
            item.quantity = cartItem.quantity;
            item.cartItemId = cartItem.id; // để xóa khỏi giỏ sau khi đặt hàng
            items.add(item);
        }
        CheckoutSession.setItems(items);
        NavHostFragment.findNavController(this).navigate(R.id.checkoutFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
