package com.example.ecommerce.ui.product;

import android.content.Intent;
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

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentProductDetailBinding;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.FavoriteStore;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.ProductDetailViewModel;
import com.example.ecommerce.viewmodel.ProductDetailViewModel.Selection;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

/**
 * Chi tiết sản phẩm: ảnh trượt, giá, chọn size và màu, số lượng, chi tiết, mô tả, thanh "Mua ngay".
 * Nhận id sản phẩm qua Bundle (khóa Constants.ARG_PRODUCT_ID).
 */
public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private ProductDetailViewModel viewModel;
    private final ImageSliderAdapter sliderAdapter = new ImageSliderAdapter();
    private Product product;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);
        viewModel.load(requireArguments().getString(Constants.ARG_PRODUCT_ID));

        binding.imagePager.setAdapter(sliderAdapter);
        new TabLayoutMediator(binding.imageDots, binding.imagePager, (tab, position) -> { }).attach();

        binding.toolbar.setAction(1, R.drawable.ic_heart, getString(R.string.favorite), v -> toggleFavorite());
        binding.toolbar.setAction(2, R.drawable.ic_share, getString(R.string.share_product), v -> shareProduct());

        binding.bottomBar.setLabel(getString(R.string.subtotal));
        binding.bottomBar.setPrimaryButton(getString(R.string.buy_now), v -> onAction());
        binding.bottomBar.setSecondaryIconButton(R.drawable.ic_cart, getString(R.string.add_to_cart), v -> onAction());

        binding.quantityStepper.setOnQuantityChangeListener(viewModel::setQuantity);

        viewModel.getProduct().observe(getViewLifecycleOwner(), this::renderProduct);
        viewModel.getSelection().observe(getViewLifecycleOwner(), this::renderSelection);
    }

    private void renderProduct(Resource<Product> resource) {
        switch (resource.status) {
            case LOADING:
                setContentVisible(false);
                binding.stateView.showLoading();
                break;
            case ERROR:
                setContentVisible(false);
                binding.stateView.showError(resource.message, v -> viewModel.reload());
                break;
            case SUCCESS:
                product = resource.data;
                binding.stateView.hide();
                setContentVisible(true);
                bindProduct(product);
                renderSelection(viewModel.getSelection().getValue());
                break;
        }
    }

    private void setContentVisible(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        binding.scroll.setVisibility(v);
        binding.bottomBar.setVisibility(v);
    }

    /** Phần thông tin không đổi theo lựa chọn của người dùng. */
    private void bindProduct(Product p) {
        binding.toolbar.setTitle(p.getBrand() != null ? p.getBrand() : p.getName());
        binding.toolbar.setSubtitle(p.getCategoryName());
        updateFavoriteIcon();

        String tag = p.getCategoryName() != null ? p.getCategoryName() : p.getCategoryTypeName();
        binding.categoryTag.setText(tag);
        binding.categoryTag.setVisibility(tag == null ? View.INVISIBLE : View.VISIBLE);
        binding.productRating.setText(Formatters.rating(p.getRating()));
        binding.productName.setText(p.getName());
        binding.productBrand.setText(p.getBrand());
        binding.productBrand.setVisibility(p.getBrand() == null ? View.GONE : View.VISIBLE);
        binding.productPrice.setText(Formatters.vnd(p.getPrice()));

        List<String> images = p.getImageUrls();
        sliderAdapter.submit(images);
        binding.imageDots.setVisibility(images.size() > 1 ? View.VISIBLE : View.INVISIBLE);

        binding.detailsContainer.removeAllViews();
        addDetailRow(getString(R.string.brand), p.getBrand());
        addDetailRow(getString(R.string.category), p.getCategoryName());
        addDetailRow(getString(R.string.category_type), p.getCategoryTypeName());

        boolean hasDescription = p.getDescription() != null && !p.getDescription().trim().isEmpty();
        binding.description.setText(p.getDescription());
        binding.description.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        binding.descriptionTitle.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
    }

    private void addDetailRow(String label, @Nullable String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        View row = getLayoutInflater().inflate(R.layout.item_detail_row, binding.detailsContainer, false);
        ((TextView) row.findViewById(R.id.row_label)).setText(label);
        ((TextView) row.findViewById(R.id.row_value)).setText(value);
        binding.detailsContainer.addView(row);
    }

    /** Vẽ lại các phần phụ thuộc lựa chọn: chip size, chip màu, tồn kho, số lượng, tạm tính. */
    private void renderSelection(@Nullable Selection s) {
        if (product == null || s == null) {
            return;
        }

        List<String> sizes = product.getSizes();
        binding.sizeSection.setVisibility(sizes.isEmpty() ? View.GONE : View.VISIBLE);
        binding.sizeGroup.removeAllViews();
        for (String size : sizes) {
            boolean checked = size.equals(s.size);
            addChip(binding.sizeGroup, size, checked,
                    checked || viewModel.isSizeAvailable(product, size), () -> viewModel.selectSize(size));
        }

        List<String> colors = product.getColors();
        binding.colorSection.setVisibility(colors.isEmpty() ? View.GONE : View.VISIBLE);
        binding.colorGroup.removeAllViews();
        for (String color : colors) {
            boolean checked = color.equals(s.color);
            addChip(binding.colorGroup, color, checked,
                    checked || viewModel.isColorAvailable(product, color), () -> viewModel.selectColor(color));
        }

        int stock = s.variant != null ? s.variant.getStockQuantity() : product.getTotalStock();
        binding.stockText.setText(stock > 0 ? getString(R.string.stock_left, stock) : getString(R.string.out_of_stock));

        binding.quantityStepper.setMax(s.getMaxQuantity());
        binding.quantityStepper.setQuantity(s.quantity);
        binding.quantityStepper.setEnabled(s.variant != null && s.variant.getStockQuantity() > 0);

        binding.bottomBar.setPrice(Formatters.vnd(product.getPrice() * s.quantity));
        binding.bottomBar.setPrimaryEnabled(!(s.variant != null && s.variant.getStockQuantity() == 0));
    }

    private void addChip(ChipGroup group, String text, boolean checked, boolean enabled, Runnable onClick) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_choice, group, false);
        chip.setText(text);
        chip.setChecked(checked);
        chip.setEnabled(enabled);
        chip.setOnClickListener(v -> onClick.run());
        group.addView(chip);
    }

    /** Mua ngay / Thêm vào giỏ: cần chọn đủ size và màu. Giỏ hàng và đặt hàng sẽ làm ở bước sau. */
    private void onAction() {
        Selection s = viewModel.getSelection().getValue();
        if (s == null || s.variant == null) {
            Toast.makeText(requireContext(), R.string.pick_variant, Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: gọi API giỏ hàng (POST /api/carts) khi làm module Giỏ hàng; hiện chỉ thông báo.
        Toast.makeText(requireContext(), R.string.feature_coming, Toast.LENGTH_SHORT).show();
    }

    private void toggleFavorite() {
        if (product == null) {
            return;
        }
        FavoriteStore.toggle(requireContext(), product.getId());
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        if (product == null) {
            return;
        }
        boolean favorite = FavoriteStore.isFavorite(requireContext(), product.getId());
        binding.toolbar.setActionIcon(1, favorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
    }

    private void shareProduct() {
        if (product == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, product.getName() + " - " + Formatters.vnd(product.getPrice()));
        startActivity(Intent.createChooser(intent, getString(R.string.share_product)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
