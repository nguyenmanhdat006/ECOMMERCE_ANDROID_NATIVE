package com.example.ecommerce.ui.order;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemOrderBinding;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Danh sách thẻ đơn hàng: mã đơn, trạng thái, sản phẩm đầu tiên, tổng tiền. */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final List<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Order order = orders.get(position);
        holder.binding.orderNumber.setText(order.orderNumber);
        holder.binding.orderStatus.setStatus(order.status);
        holder.binding.orderDate.setText(Formatters.date(order.orderDate));
        holder.binding.orderTotal.setText(Formatters.vnd(order.totalAmount));

        // Chỉ hiện sản phẩm đầu tiên; các sản phẩm còn lại ghi gọn "và N sản phẩm khác"
        int count = order.orderItems.size();
        if (count > 0) {
            Order.OrderItem first = order.orderItems.get(0);
            holder.binding.firstItem.bind(first.thumbnail, first.productName,
                    "Số lượng: " + first.quantity, Formatters.vnd(first.unitPrice));
        }
        if (count > 1) {
            holder.binding.moreItems.setVisibility(View.VISIBLE);
            holder.binding.moreItems.setText("và " + (count - 1) + " sản phẩm khác");
        } else {
            holder.binding.moreItems.setVisibility(View.GONE);
        }
        holder.binding.itemCount.setText(count + " sản phẩm");

        holder.binding.getRoot().setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemOrderBinding binding;

        ViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
