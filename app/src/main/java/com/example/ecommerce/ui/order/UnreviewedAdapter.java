package com.example.ecommerce.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemUnreviewedBinding;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Danh sách sản phẩm đã mua nhưng chưa đánh giá, mỗi dòng có nút "Đánh giá". */
public class UnreviewedAdapter extends RecyclerView.Adapter<UnreviewedAdapter.ViewHolder> {

    public interface OnReviewClickListener {
        void onReviewClick(Order.OrderItem item);
    }

    private final List<Order.OrderItem> items = new ArrayList<>();
    private final OnReviewClickListener listener;

    public UnreviewedAdapter(OnReviewClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Order.OrderItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemUnreviewedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Order.OrderItem item = items.get(position);
        holder.binding.lineItem.bind(item.thumbnail, item.productName,
                "Số lượng: " + item.quantity, Formatters.vnd(item.unitPrice));
        holder.binding.reviewButton.setOnClickListener(v -> listener.onReviewClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemUnreviewedBinding binding;

        ViewHolder(ItemUnreviewedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
