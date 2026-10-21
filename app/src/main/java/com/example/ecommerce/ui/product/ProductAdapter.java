package com.example.ecommerce.ui.product;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.databinding.ItemProductBinding;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.util.FavoriteStore;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.ImageUrls;

/**
 * C7 - Thẻ sản phẩm (ô lưới): ảnh, nhãn giá, tim yêu thích, tồn kho, điểm đánh giá, tên.
 * Dùng cho Trang chủ, Danh mục, Tìm kiếm.
 */
public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product, OnProductClickListener listener) {
            binding.productName.setText(product.getName());
            binding.productPrice.setText(Formatters.vnd(product.getPrice()));
            binding.productRating.setText(Formatters.rating(product.getRating()));

            int stock = product.getTotalStock();
            if (stock > 0) {
                binding.productStock.setText(
                        binding.getRoot().getContext().getString(R.string.stock_left, stock));
                binding.productStock.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.danger));
            } else {
                binding.productStock.setText(R.string.out_of_stock);
                binding.productStock.setTextColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.text_hint));
            }

            Glide.with(binding.productImage)
                    .load(ImageUrls.fix(product.getCoverUrl()))
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .into(binding.productImage);

            renderFavorite(FavoriteStore.isFavorite(binding.getRoot().getContext(), product.getId()));
            binding.productFavorite.setOnClickListener(v ->
                    renderFavorite(FavoriteStore.toggle(v.getContext(), product.getId())));

            binding.getRoot().setOnClickListener(v -> listener.onProductClick(product));
        }

        private void renderFavorite(boolean favorite) {
            binding.productFavorite.setImageResource(favorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            int color = ContextCompat.getColor(binding.getRoot().getContext(),
                    favorite ? R.color.danger : R.color.text_secondary);
            ImageViewCompat.setImageTintList(binding.productFavorite, ColorStateList.valueOf(color));
        }
    }

    private static final DiffUtil.ItemCallback<Product> DIFF = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getTotalStock() == newItem.getTotalStock();
        }
    };
}
