package com.example.ecommerce.ui.product;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentProductDetailBinding;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.ui.common.ReviewItemView;
import com.example.ecommerce.util.AuthGuard;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.FavoriteStore;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.util.SessionManager;
import com.example.ecommerce.viewmodel.CartViewModel;
import com.example.ecommerce.viewmodel.ProductDetailViewModel;
import com.example.ecommerce.viewmodel.ProductDetailViewModel.Selection;
import com.example.ecommerce.viewmodel.ReviewViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * Chi tiết sản phẩm: ảnh trượt + header nổi (back, giỏ), giá/tên/thương hiệu/điểm, hàng "Phân loại",
 * mô tả thu gọn, đánh giá nổi bật, thanh "Thêm vào giỏ" + "Mua ngay".
 * Hàng Phân loại và cả hai nút ở đáy đều mở {@link VariantSheetFragment}.
 * Nhận id sản phẩm qua Bundle (khóa Constants.ARG_PRODUCT_ID).
 */
public class ProductDetailFragment extends Fragment implements VariantSheetFragment.Listener {

    private static final String TAG_SHEET = "variant_sheet";
    /** Câu BaseRepository trả về khi API báo 404. */
    private static final String NOT_FOUND = "Không tìm thấy dữ liệu.";
    private static final int DESCRIPTION_LINES = 3;
    private static final int PREVIEW_REVIEWS = 2;

    private FragmentProductDetailBinding binding;
    private ProductDetailViewModel viewModel;
    private CartViewModel cartViewModel;
    private ReviewViewModel reviewViewModel;
    private final ImageSliderAdapter sliderAdapter = new ImageSliderAdapter();
    private Product product;
    private String productId;
    private List<Review> reviews = new ArrayList<>();
    private boolean descriptionExpanded;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        productId = requireArguments().getString(Constants.ARG_PRODUCT_ID);
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        viewModel.load(productId);

        binding.imagePager.setAdapter(sliderAdapter);
        new TabLayoutMediator(binding.imageDots, binding.imagePager, (tab, position) -> { }).attach();

        binding.backButton.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.cartButton.setOnClickListener(v -> selectBottomTab(R.id.cartFragment));
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
        binding.shareButton.setOnClickListener(v -> shareProduct());
        binding.variantRow.setOnClickListener(v -> openVariantSheet(VariantSheetFragment.Mode.ADD));
        binding.descriptionToggle.setOnClickListener(v -> {
            descriptionExpanded = !descriptionExpanded;
            applyDescriptionState();
        });
        binding.reviewSeeAll.setOnClickListener(v -> openReviews());

        binding.bottomBar.setButtonsOnly(true);
        binding.bottomBar.setSecondaryButton(getString(R.string.add_to_cart), R.drawable.ic_cart,
                v -> openVariantSheet(VariantSheetFragment.Mode.ADD));
        binding.bottomBar.setPrimaryButton(getString(R.string.buy_now),
                v -> openVariantSheet(VariantSheetFragment.Mode.BUY));

        viewModel.getProduct().observe(getViewLifecycleOwner(), this::renderProduct);
        viewModel.getSelection().observe(getViewLifecycleOwner(), this::renderSelection);
        reviewViewModel.getReviews().observe(getViewLifecycleOwner(), this::renderReviewResult);
        cartViewModel.getCart().observe(getViewLifecycleOwner(), result -> {
            if (result != null && result.status == Resource.Status.SUCCESS) {
                List<CartItem> items = result.data;
                binding.cartButton.setCount(items == null ? 0 : items.size());
            }
        });
    }

    /** Mỗi lần quay lại màn hình: cập nhật đánh giá (có thể vừa viết) và số lượng trong giỏ. */
    @Override
    public void onResume() {
        super.onResume();
        reviewViewModel.load(productId);
        if (SessionManager.isLoggedIn()) {
            cartViewModel.reloadQuietly();
        } else {
            binding.cartButton.setCount(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ---------------------------------------------------------------- trạng thái (khối 7)

    private void renderProduct(Resource<Product> resource) {
        switch (resource.status) {
            case LOADING:
                setContentVisible(false);
                binding.stateView.showLoading();
                break;
            case ERROR:
                setContentVisible(false);
                if (NOT_FOUND.equals(resource.message)) {
                    binding.stateView.showEmpty(getString(R.string.product_not_found));
                } else {
                    binding.stateView.showError(resource.message, v -> viewModel.reload());
                }
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

    // ---------------------------------------------------------------- khối 1, 2, 4

    private void bindProduct(Product p) {
        updateFavoriteIcon();

        List<String> images = p.getImageUrls();
        sliderAdapter.submit(images);
        binding.imageDots.setVisibility(images.size() > 1 ? View.VISIBLE : View.INVISIBLE);

        binding.productPrice.setText(Formatters.vnd(p.getPrice()));
        binding.productName.setText(p.getName());
        boolean hasBrand = p.getBrand() != null && !p.getBrand().isEmpty();
        binding.productBrand.setText(hasBrand ? getString(R.string.brand_label, p.getBrand()) : "");
        binding.productBrand.setVisibility(hasBrand ? View.VISIBLE : View.GONE);
        binding.brandDot.setVisibility(hasBrand ? View.VISIBLE : View.GONE);
        renderRating();

        String description = p.getDescription();
        boolean hasDescription = description != null && !description.trim().isEmpty();
        binding.descriptionCard.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        if (hasDescription) {
            binding.description.setText(description.trim());
            applyDescriptionState();
        }
    }

    /** "★ 4.6 (128 đánh giá)"; chưa có điểm lẫn đánh giá thì hiện "Chưa có đánh giá" (không có sao). */
    private void renderRating() {
        if (binding == null || product == null) {
            return;
        }
        int count = reviews.size();
        Double rating = product.getRating();
        boolean hasRating = rating != null && rating > 0;
        if (!hasRating && count == 0) {
            binding.ratingStar.setVisibility(View.GONE);
            binding.productRating.setText(R.string.no_reviews);
            binding.productRating.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            binding.productRating.getPaint().setFakeBoldText(false);
            return;
        }
        binding.ratingStar.setVisibility(View.VISIBLE);
        binding.productRating.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        binding.productRating.getPaint().setFakeBoldText(true);
        binding.productRating.setText(count > 0
                ? getString(R.string.rating_with_count, Formatters.rating(rating), count)
                : Formatters.rating(rating));
    }

    /** Mô tả thu gọn 3 dòng; chỉ hiện "Xem thêm" khi nội dung thật sự bị cắt. */
    private void applyDescriptionState() {
        TextView text = binding.description;
        text.setMaxLines(descriptionExpanded ? Integer.MAX_VALUE : DESCRIPTION_LINES);
        text.setEllipsize(descriptionExpanded ? null : TextUtils.TruncateAt.END);
        text.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                ViewTreeObserver observer = text.getViewTreeObserver();
                if (observer.isAlive()) {
                    observer.removeOnPreDrawListener(this);
                }
                if (binding != null) {
                    Layout layout = text.getLayout();
                    int lines = layout == null ? 0 : layout.getLineCount();
                    boolean truncated = lines > 0 && layout.getEllipsisCount(lines - 1) > 0;
                    binding.descriptionToggle.setVisibility(
                            descriptionExpanded || truncated ? View.VISIBLE : View.GONE);
                    binding.descriptionToggle.setText(descriptionExpanded ? R.string.see_less : R.string.see_more);
                }
                return true;
            }
        });
    }

    // ---------------------------------------------------------------- khối 3 và 6

    private void renderSelection(@Nullable Selection s) {
        if (binding == null || product == null || s == null) {
            return;
        }
        boolean soldOut = product.getVariants().isEmpty() || product.getTotalStock() <= 0;
        boolean hasColors = !product.getColors().isEmpty();
        boolean hasSizes = !product.getSizes().isEmpty();

        String value;
        int colorRes = R.color.text_secondary;
        if (soldOut) {
            value = getString(R.string.out_of_stock);
            colorRes = R.color.text_hint;
        } else if (s.color == null && s.size == null) {
            value = defaultVariantText(hasColors, hasSizes);
        } else {
            // Đã chọn một phần hoặc đủ: "Trắng, M" hoặc "Trắng, Size"
            List<String> parts = new ArrayList<>();
            if (hasColors) {
                parts.add(s.color != null ? s.color : getString(R.string.color));
            }
            if (hasSizes) {
                parts.add(s.size != null ? s.size : getString(R.string.size));
            }
            value = TextUtils.join(", ", parts);
            colorRes = R.color.primary;
        }
        binding.variantValue.setText(value);
        binding.variantValue.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        binding.variantRow.setEnabled(!soldOut);

        binding.bottomBar.setPrimaryEnabled(!soldOut);
        binding.bottomBar.setSecondaryEnabled(!soldOut);
    }

    private String defaultVariantText(boolean hasColors, boolean hasSizes) {
        if (hasColors && hasSizes) {
            return getString(R.string.color) + ", " + getString(R.string.size);
        }
        if (hasColors) {
            return getString(R.string.color);
        }
        if (hasSizes) {
            return getString(R.string.size);
        }
        return getString(R.string.variant_default);
    }

    private void openVariantSheet(VariantSheetFragment.Mode mode) {
        if (product == null || getChildFragmentManager().findFragmentByTag(TAG_SHEET) != null) {
            return;
        }
        VariantSheetFragment.newInstance(mode).show(getChildFragmentManager(), TAG_SHEET);
    }

    // ---------------------------------------------------------------- khối 5: đánh giá nổi bật

    /** Chỉ cập nhật khi tải xong; đang tải hoặc lỗi thì giữ nguyên cái đang hiện. */
    private void renderReviewResult(@Nullable Resource<List<Review>> result) {
        if (result == null || result.status != Resource.Status.SUCCESS) {
            return;
        }
        reviews = result.data == null ? new ArrayList<>() : result.data;
        renderRating();
        renderReviews();
    }

    private void renderReviews() {
        if (binding == null) {
            return;
        }
        int count = reviews.size();
        binding.reviewCard.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        binding.reviewContainer.removeAllViews();
        if (count == 0) {
            return;
        }
        binding.reviewTitle.setText(getString(R.string.reviews_count, count));
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < Math.min(PREVIEW_REVIEWS, count); i++) {
            if (i > 0) {
                View divider = new View(requireContext());
                divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.stroke));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (int) density);
                params.topMargin = (int) (12 * density);
                params.bottomMargin = (int) (12 * density);
                binding.reviewContainer.addView(divider, params);
            }
            Review review = reviews.get(i);
            ReviewItemView item = new ReviewItemView(requireContext());
            item.bind(review.user == null ? "" : review.user.getFullName(),
                    review.rating, review.comment, Formatters.date(review.createdAt));
            binding.reviewContainer.addView(item, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void openReviews() {
        if (product == null) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, product.getId());
        args.putString(Constants.ARG_PRODUCT_NAME, product.getName());
        NavHostFragment.findNavController(this).navigate(R.id.reviewListFragment, args);
    }

    // ---------------------------------------------------------------- yêu thích, chia sẻ

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
        binding.favoriteButton.setImageResource(favorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
        ImageViewCompat.setImageTintList(binding.favoriteButton, ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), favorite ? R.color.danger : R.color.text_secondary)));
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

    // ---------------------------------------------------------------- VariantSheetFragment.Listener

    @Override
    public void onAddedToCart() {
        if (binding == null) {
            return;
        }
        Snackbar.make(binding.getRoot(), R.string.added_to_cart, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.bottomBar)
                .show();
    }

    /** "Mua ngay": bỏ qua giỏ hàng, sang thẳng Thanh toán với biến thể và số lượng đã chọn. */
    @Override
    public void onBuyNow() {
        Selection s = viewModel.getSelection().getValue();
        if (product == null || s == null || s.variant == null) {
            return;
        }
        CheckoutSession.Item item = new CheckoutSession.Item();
        item.variantId = s.variant.getId();
        item.productName = product.getName();
        item.imageUrl = product.getCoverUrl();
        item.variantText = s.color + ", " + s.size; // cùng thứ tự "màu, size" như giỏ hàng
        item.price = product.getPrice();
        item.quantity = s.quantity;
        item.cartItemId = null; // không đi qua giỏ hàng

        List<CheckoutSession.Item> items = new ArrayList<>();
        items.add(item);
        CheckoutSession.setItems(items);
        NavHostFragment.findNavController(this).navigate(R.id.checkoutFragment);
    }

    @Override
    public void onLoginRequired() {
        AuthGuard.requireLogin(this); // chưa đăng nhập: báo và mở màn Đăng nhập
    }

    private void selectBottomTab(int tabId) {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView nav =
                    getActivity().findViewById(R.id.bottom_nav);
            if (nav != null) {
                nav.setSelectedItemId(tabId);
            }
        }
    }
}