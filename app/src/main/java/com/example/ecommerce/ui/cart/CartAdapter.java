package com.example.ecommerce.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemCartBinding;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Danh sách các dòng trong giỏ hàng: mỗi dòng có ảnh, tên, đơn giá, bộ chỉnh số lượng và nút Xóa. */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onRemove(CartItem item);
    }

    private final List<CartItem> items = new ArrayList<>();
    private final OnCartActionListener listener;

    public CartAdapter(OnCartActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<CartItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CartItem item = items.get(position);

        holder.binding.lineItem.bind(item.product.getCoverUrl(), item.product.getName(),
                item.getVariantText(), Formatters.vnd(item.product.getPrice()));

        // Số lượng tối đa bằng tồn kho của biến thể
        holder.binding.quantityStepper.setMax(item.productVariant.getStockQuantity());
        holder.binding.quantityStepper.setQuantity(item.quantity);
        holder.binding.quantityStepper.setOnQuantityChangeListener(
                quantity -> listener.onQuantityChanged(item, quantity));

        holder.binding.removeButton.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;

        ViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
