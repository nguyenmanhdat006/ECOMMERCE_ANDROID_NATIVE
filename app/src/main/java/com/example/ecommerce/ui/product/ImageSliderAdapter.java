package com.example.ecommerce.ui.product;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.util.ImageUrls;

import java.util.ArrayList;
import java.util.List;

/** Adapter cho ViewPager2 trượt ảnh sản phẩm. */
public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {

    private final List<String> urls = new ArrayList<>();

    public void submit(List<String> newUrls) {
        urls.clear();
        urls.addAll(newUrls);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView view = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slider_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.image)
                .load(ImageUrls.fix(urls.get(position)))
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;

        ViewHolder(ImageView image) {
            super(image);
            this.image = image;
        }
    }
}
