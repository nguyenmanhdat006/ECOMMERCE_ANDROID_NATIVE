package com.example.ecommerce.ui.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemCategoryCardBinding;
import com.example.ecommerce.model.Category;

import java.util.ArrayList;
import java.util.List;

/** Lưới các thẻ danh mục ở màn Danh mục. Bấm một thẻ thì báo cho Fragment. */
public class CategoryCardAdapter extends RecyclerView.Adapter<CategoryCardAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;

    public CategoryCardAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Category> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemCategoryCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Category category = categories.get(position);
        holder.binding.categoryName.setText(category.getName());

        // Ghép tên các loại con thành một dòng: "Áo thun, Áo sơ mi, Quần jean"
        StringBuilder types = new StringBuilder();
        for (Category.CategoryType type : category.getCategoryTypes()) {
            if (types.length() > 0) {
                types.append(", ");
            }
            types.append(type.name);
        }
        holder.binding.categoryTypes.setText(types.toString());

        holder.binding.getRoot().setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCategoryCardBinding binding;

        ViewHolder(ItemCategoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
