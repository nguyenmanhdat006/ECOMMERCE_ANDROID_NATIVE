package com.example.ecommerce.ui.review;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.databinding.ItemReviewBinding;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.model.User;
import com.example.ecommerce.util.Formatters;
import com.example.ecommerce.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/** Danh sách đánh giá. Đánh giá của chính người đang đăng nhập có thêm nút Sửa và Xóa. */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    public interface OnReviewActionListener {
        void onEdit(Review review);

        void onDelete(Review review);
    }

    private final List<Review> reviews = new ArrayList<>();
    private final OnReviewActionListener listener;

    public ReviewAdapter(OnReviewActionListener listener) {
        this.listener = listener;
    }

    public void submit(List<Review> newReviews) {
        reviews.clear();
        reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Review review = reviews.get(position);
        String name = (review.user == null) ? "" : review.user.getFullName();
        holder.binding.reviewView.bind(name, review.rating, review.comment, Formatters.date(review.createdAt));

        // Có phải đánh giá của mình không?
        User me = SessionManager.getUser();
        boolean isMine = me != null && review.user != null && me.id.equals(review.user.id);
        holder.binding.ownActions.setVisibility(isMine ? View.VISIBLE : View.GONE);
        holder.binding.editButton.setOnClickListener(v -> listener.onEdit(review));
        holder.binding.deleteButton.setOnClickListener(v -> listener.onDelete(review));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemReviewBinding binding;

        ViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
