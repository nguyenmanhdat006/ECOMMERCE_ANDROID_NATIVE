package com.example.ecommerce.ui.product;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.databinding.SheetVariantBinding;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductVariant;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.ImageUrls;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;
import com.example.ecommerce.viewmodel.CartViewModel;
import com.example.ecommerce.viewmodel.ProductDetailViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.function.Consumer;

/**
 * Bottom sheet chọn biến thể (màu, size, số lượng). Mở từ Chi tiết sản phẩm bằng
 * {@code VariantSheetFragment.newInstance(mode).show(getChildFragmentManager(), tag)}; Fragment cha phải implement
 * {@link Listener}. Lựa chọn nằm trong ProductDetailViewModel của Fragment cha nên không mất khi đóng/mở lại sheet.
 *
 * - Mode.ADD: nút "Thêm vào giỏ" gọi POST /api/carts rồi đóng sheet.
 * - Mode.BUY: nút "Mua ngay" đóng sheet và báo cho Fragment cha chuyển sang Thanh toán.
 * - Chưa đăng nhập: đóng sheet và báo cho Fragment cha chuyển sang Đăng nhập.
 */
public class VariantSheetFragment extends BottomSheetDialogFragment {

    public enum Mode { ADD, BUY }

    public interface Listener {
        void onAddedToCart();

        void onBuyNow();

        void onLoginRequired();
    }

    private static final String ARG_MODE = "mode";

    private SheetVariantBinding binding;
    private ProductDetailViewModel viewModel;
    private CartViewModel cartViewModel;
    private Mode mode = Mode.ADD;

    public static VariantSheetFragment newInstance(Mode mode) {
        VariantSheetFragment fragment = new VariantSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetVariantBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            mode = Mode.valueOf(args.getString(ARG_MODE, Mode.ADD.name()));
        }
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProductDetailViewModel.class);
        cartViewModel = new ViewModelProvider(requireParentFragment()).get(CartViewModel.class);

        binding.sheetClose.setOnClickListener(v -> dismiss());
        binding.sheetStepper.setOnQuantityChangeListener(viewModel::setQuantity);
        binding.sheetConfirm.setText(getString(mode == Mode.BUY ? R.string.buy_now : R.string.add_to_cart));
        binding.sheetConfirm.setOnClickListener(v -> onConfirm());

        viewModel.getProduct().observe(getViewLifecycleOwner(), resource -> render());
        viewModel.getSelection().observe(getViewLifecycleOwner(), selection -> render());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog sheetDialog = (BottomSheetDialog) dialog;
            int sheetId = getResources().getIdentifier("design_bottom_sheet", "id", "com.google.android.material");
            View sheet = sheetId != 0 ? sheetDialog.findViewById(sheetId) : null;
            if (sheet != null) {
                sheet.setBackgroundColor(Color.TRANSPARENT);
            }
            sheetDialog.getBehavior().setSkipCollapsed(true);
            sheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void render() {
        Resource<Product> resource = viewModel.getProduct().getValue();
        ProductDetailViewModel.Selection selection = viewModel.getSelection().getValue();
        if (binding == null || resource == null || resource.data == null || selection == null) {
            return;
        }
        Product product = resource.data;

        // 1. Đầu sheet: ảnh, giá, tồn kho. Giá chỉ có một mức vì backend không có giá riêng cho từng biến thể.
        List<String> images = product.getImageUrls();
        Glide.with(binding.sheetImage)
                .load(ImageUrls.fix(images.isEmpty() ? product.getCoverUrl() : images.get(0)))
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(binding.sheetImage);
        binding.sheetPrice.setText(Formatters.vnd(product.getPrice()));
        ProductVariant variant = selection.variant;
        int stock = variant != null ? variant.getStockQuantity() : product.getTotalStock();
        binding.sheetStock.setText(stock > 0 ? getString(R.string.stock_left, stock) : getString(R.string.out_of_stock));

        // 2, 3. Màu và size
        List<String> colors = product.getColors();
        List<String> sizes = product.getSizes();
        binding.colorSection.setVisibility(colors.isEmpty() ? View.GONE : View.VISIBLE);
        binding.sizeSection.setVisibility(sizes.isEmpty() ? View.GONE : View.VISIBLE);
        fillOptions(binding.colorGroup, colors, selection.color, false,
                value -> viewModel.isColorAvailable(product, value), viewModel::selectColor);
        fillOptions(binding.sizeGroup, sizes, selection.size, true,
                value -> viewModel.isSizeAvailable(product, value), viewModel::selectSize);

        // 4. Số lượng: tối thiểu 1, tối đa bằng tồn kho của biến thể
        boolean canBuy = variant != null && variant.getStockQuantity() > 0;
        binding.sheetStepper.setMax(Math.max(1, selection.getMaxQuantity()));
        binding.sheetStepper.setQuantity(selection.quantity);
        binding.sheetStepper.setEnabled(canBuy);

        // 5. Nút xác nhận
        boolean soldOut = product.getVariants().isEmpty() || product.getTotalStock() <= 0;
        binding.sheetConfirm.setEnabled(!soldOut);
    }

    /**
     * Dựng lại hàng nút chọn. Nút hết hàng bị làm mờ và không bấm được; với size còn gạch ngang.
     * Nút đang chọn luôn bấm được để bỏ chọn.
     */
    private void fillOptions(ChipGroup group, List<String> values, @Nullable String selected, boolean strikeWhenDisabled,
                             java.util.function.Predicate<String> isAvailable, Consumer<String> onClick) {
        group.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(group.getContext());
        for (String value : values) {
            TextView pill = (TextView) inflater.inflate(R.layout.item_option_pill, group, false);
            boolean checked = value.equals(selected);
            boolean enabled = checked || isAvailable.test(value);
            pill.setText(value);
            pill.setSelected(checked);
            pill.setEnabled(enabled);
            if (strikeWhenDisabled && !enabled) {
                pill.setPaintFlags(pill.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                pill.setPaintFlags(pill.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            pill.setOnClickListener(v -> onClick.accept(value));
            group.addView(pill);
        }
    }

    private void onConfirm() {
        ProductDetailViewModel.Selection selection = viewModel.getSelection().getValue();
        if (selection == null) {
            return;
        }
        ProductVariant variant = selection.variant;
        if (variant == null) {
            toast(getString(R.string.pick_variant)); // chưa chọn đủ màu và size
            return;
        }
        if (variant.getStockQuantity() <= 0) {
            toast(getString(R.string.out_of_stock));
            return;
        }
        Listener listener = listener();
        if (!SessionManager.isLoggedIn()) {
            dismiss();
            if (listener != null) {
                listener.onLoginRequired();
            }
            return;
        }
        String variantId = variant.getId();
        int quantity = selection.quantity;
        if (mode == Mode.BUY) {
            dismiss();
            if (listener != null) {
                listener.onBuyNow();
            }
            return;
        }

        binding.sheetConfirm.setLoading(true);
        cartViewModel.addToCart(variantId, quantity, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (listener != null) {
                    listener.onAddedToCart();
                }
                if (isAdded()) {
                    dismissAllowingStateLoss();
                }
            }

            @Override
            public void onError(String message) {
                if (binding == null) {
                    return;
                }
                binding.sheetConfirm.setLoading(false);
                toast(message);
            }
        });
    }

    @Nullable
    private Listener listener() {
        Fragment parent = getParentFragment();
        return parent instanceof Listener ? (Listener) parent : null;
    }

    private void toast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }
}