package com.example.ecommerce.ui.cart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentCartBinding;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;
import com.example.ecommerce.viewmodel.CartViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab Giỏ hàng: tiêu đề "Giỏ hàng (N)", danh sách dòng (đổi số lượng, xóa bằng thùng rác hoặc vuốt trái),
 * tạm tính, thanh "Tổng cộng + Mua hàng". Giỏ trống có nút "Đi mua sắm"; chưa đăng nhập hiện lời mời đăng nhập.
 */
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

        binding.toolbar.setShowBack(false); // là màn hình tab nên không có nút quay lại
        binding.toolbar.setTitle(getString(R.string.cart_title));

        adapter = new CartAdapter(new CartAdapter.OnCartActionListener() {
            @Override
            public void onItemClick(CartItem item) {
                openProduct(item);
            }

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
        binding.cartList.setItemAnimator(null); // tránh nháy dòng khi đổi số lượng liên tục
        new ItemTouchHelper(new SwipeToDelete()).attachToRecyclerView(binding.cartList);

        binding.loginPrompt.setMessage("Đăng nhập để xem giỏ hàng của bạn");
        binding.loginPrompt.setOnLoginClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.loginFragment));

        binding.bottomBar.setLabel(getString(R.string.total));
        binding.bottomBar.setPrimaryButton(getString(R.string.place_order), v -> goToCheckout());

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

    // ---------------------------------------------------------------- hiển thị

    private void showLoginPrompt() {
        showContent(false);
        binding.stateView.hide();
        binding.toolbar.setTitle(getString(R.string.cart_title));
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
                showContent(false);
                binding.toolbar.setTitle(getString(R.string.cart_title));
                binding.stateView.showError(result.message, v -> viewModel.load());
                break;
        }
    }

    private void showCart(List<CartItem> items) {
        currentItems = (items == null) ? new ArrayList<CartItem>() : items;
        if (currentItems.isEmpty()) {
            showContent(false);
            binding.toolbar.setTitle(getString(R.string.cart_title));
            binding.stateView.showAction(getString(R.string.cart_empty), R.drawable.ic_cart,
                    getString(R.string.go_shopping), v -> openHome());
            return;
        }
        binding.stateView.hide();
        showContent(true);
        binding.toolbar.setTitle(getString(R.string.cart_title_count, currentItems.size()));
        adapter.submit(currentItems);

        // Tạm tính = tổng thành tiền các dòng
        double total = 0;
        for (CartItem item : currentItems) {
            total += item.getLineTotal();
        }
        String totalText = Formatters.vnd(total);
        binding.subtotalValue.setText(totalText);
        binding.bottomBar.setPrice(totalText);
    }

    /** Hiện hoặc ẩn các khối có dữ liệu: danh sách + tạm tính và thanh hành động dưới. */
    private void showContent(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        binding.scroll.setVisibility(v);
        binding.bottomBar.setVisibility(v);
    }

    // ---------------------------------------------------------------- hành động

    private void confirmRemove(final CartItem item) {
        ConfirmDialog.show(requireContext(), getString(R.string.delete_item_title),
                getString(R.string.delete_item_message), getString(R.string.delete), true,
                () -> viewModel.remove(item));
    }

    /** Bấm vào một dòng: mở chi tiết sản phẩm. */
    private void openProduct(CartItem item) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, item.product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.productDetailFragment, args);
    }

    /** "Đi mua sắm": về tab Trang chủ bằng thanh điều hướng dưới. */
    private void openHome() {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView nav =
                    getActivity().findViewById(R.id.bottom_nav);
            if (nav != null) {
                nav.setSelectedItemId(R.id.homeFragment);
            }
        }
    }

    /** Chuyển các dòng trong giỏ sang dữ liệu mua hàng rồi mở màn Thanh toán. */
    private void goToCheckout() {
        List<CheckoutSession.Item> items = new ArrayList<>();
        for (CartItem cartItem : currentItems) {
            CheckoutSession.Item item = new CheckoutSession.Item();
            item.variantId = cartItem.productVariant.getId();
            item.productName = cartItem.product.getName();
            item.imageUrl = cartItem.product.getCoverUrl();
            item.variantText = cartItem.getVariantLabel();
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

    // ---------------------------------------------------------------- vuốt trái để xóa

    private class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);

        SwipeToDelete() {
            super(0, ItemTouchHelper.LEFT);
            paint.setColor(ContextCompat.getColor(requireContext(), R.color.danger_bg));
            if (icon != null) {
                icon.setTint(ContextCompat.getColor(requireContext(), R.color.danger));
            }
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            CartItem item = adapter.itemAt(position);
            adapter.notifyItemChanged(position); // trả dòng về chỗ cũ; xóa thật chỉ khi người dùng xác nhận
            confirmRemove(item);
        }

        @Override
        public void onChildDraw(@NonNull Canvas canvas, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder holder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View item = holder.itemView;
            if (dX < 0) {
                float density = item.getResources().getDisplayMetrics().density;
                float radius = 16 * density;
                canvas.drawRoundRect(new RectF(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom()),
                        radius, radius, paint);
                if (icon != null) {
                    int size = (int) (24 * density);
                    int right = item.getRight() - (int) (20 * density);
                    int top = item.getTop() + (item.getHeight() - size) / 2;
                    if (-dX > size + 20 * density) {
                        icon.setBounds(right - size, top, right, top + size);
                        icon.draw(canvas);
                    }
                }
            }
            super.onChildDraw(canvas, rv, holder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}