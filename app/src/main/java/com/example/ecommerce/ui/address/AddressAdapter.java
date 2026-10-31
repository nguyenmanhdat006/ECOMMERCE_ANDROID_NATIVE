package com.example.ecommerce.ui.address;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemAddressBinding;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.util.CheckoutSession;

import java.util.ArrayList;
import java.util.List;

/** Danh sách thẻ địa chỉ, mỗi thẻ có nút Sửa và Xóa. */
public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    public interface OnAddressActionListener {
        void onClick(Address address);

        void onEdit(Address address);

        void onDelete(Address address);
    }

    private final List<Address> addresses = new ArrayList<>();
    private final OnAddressActionListener listener;

    public AddressAdapter(OnAddressActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Address> newAddresses) {
        addresses.clear();
        addresses.addAll(newAddresses);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemAddressBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Address address = addresses.get(position);

        // Địa chỉ đang được chọn để giao hàng thì hiện nhãn "Đang chọn"
        Address selected = CheckoutSession.getSelectedAddress();
        boolean isSelected = selected != null && selected.id.equals(address.id);
        holder.binding.addressCard.bind(address.getDisplayName(), address.phoneNumber,
                address.getFullLine(), isSelected);

        holder.binding.addressCard.setOnClickListener(v -> listener.onClick(address));
        holder.binding.editButton.setOnClickListener(v -> listener.onEdit(address));
        holder.binding.deleteButton.setOnClickListener(v -> listener.onDelete(address));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAddressBinding binding;

        ViewHolder(ItemAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
