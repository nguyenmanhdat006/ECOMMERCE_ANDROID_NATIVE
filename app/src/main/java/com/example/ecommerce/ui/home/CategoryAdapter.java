package com.example.ecommerce.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Hàng chip danh mục cuộn ngang. Chip đầu tiên luôn là "Tất cả" (categoryId = null).
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        /** categoryId = null nghĩa là chọn "Tất cả". */
        void onCategoryClick(@Nullable String categoryId);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    @Nullable private String selectedId;

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Category> newCategories, @Nullable String selectedCategoryId) {
        categories.clear();
        categories.addAll(newCategories);
        selectedId = selectedCategoryId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_chip, parent, false);
        return new ViewHolder((TextView) view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String id = position == 0 ? null : categories.get(position - 1).getId();
        String name = position == 0
                ? holder.view.getContext().getString(R.string.all_products)
                : categories.get(position - 1).getName();
        holder.view.setText(name);
        holder.view.setSelected(id == null ? selectedId == null : id.equals(selectedId));
        holder.view.setOnClickListener(v -> {
            selectedId = id;
            notifyDataSetChanged();
            listener.onCategoryClick(id);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView view;

        ViewHolder(TextView view) {
            super(view);
            this.view = view;
        }
    }
}
