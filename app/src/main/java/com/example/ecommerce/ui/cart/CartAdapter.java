package com.example.ecommerce.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemCartBinding;
import com.example.ecommerce.databinding.ItemCartLineEndBinding;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách các dòng trong giỏ hàng. Mỗi dòng là C8 (LineItemView): ảnh, tên, phân loại, đơn giá;
 * ô bên phải có nút thùng rác và bộ chỉnh số lượng C9 (tối đa bằng tồn kho của biến thể).
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartActionListener {
        void onItemClick(CartItem item);

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

    /** Dùng khi vuốt để xóa: lấy dòng đang ở vị trí đó. */
    public CartItem itemAt(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCartBinding binding = ItemCartBinding.inflate(inflater, parent, false);
        // Gắn nút thùng rác + bộ chỉnh số lượng vào ô bên phải của LineItemView
        ItemCartLineEndBinding end = ItemCartLineEndBinding.inflate(inflater, binding.lineItem.getEndContainer(), true);
        return new ViewHolder(binding, end);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CartItem item = items.get(position);

        holder.binding.lineItem.bind(item.product.getCoverUrl(), item.product.getName(),
                item.getVariantLabel(), Formatters.vnd(item.product.getPrice()));

        // Đặt giá trị trước rồi mới gắn listener để dựng lại dòng không bắn sự kiện giả
        holder.end.cartStepper.setOnQuantityChangeListener(null);
        holder.end.cartStepper.setMax(Math.max(1, item.productVariant.getStockQuantity()));
        holder.end.cartStepper.setQuantity(item.quantity);
        holder.end.cartStepper.setOnQuantityChangeListener(
                quantity -> listener.onQuantityChanged(item, quantity));

        holder.end.cartDelete.setOnClickListener(v -> listener.onRemove(item));
        holder.binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCartBinding binding;
        final ItemCartLineEndBinding end;

        ViewHolder(ItemCartBinding binding, ItemCartLineEndBinding end) {
            super(binding.getRoot());
            this.binding = binding;
            this.end = end;
        }
    }
}