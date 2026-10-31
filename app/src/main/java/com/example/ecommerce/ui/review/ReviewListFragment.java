package com.example.ecommerce.ui.review;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentReviewListBinding;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.ReviewViewModel;

import java.util.List;
import java.util.Locale;

/** Danh sách đánh giá của một sản phẩm, kèm điểm trung bình. */
public class ReviewListFragment extends Fragment {

    private FragmentReviewListBinding binding;
    private ReviewViewModel viewModel;
    private ReviewAdapter adapter;
    private String productId;
    private String productName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        productId = requireArguments().getString(Constants.ARG_PRODUCT_ID);
        productName = requireArguments().getString(Constants.ARG_PRODUCT_NAME);

        adapter = new ReviewAdapter(new ReviewAdapter.OnReviewActionListener() {
            @Override
            public void onEdit(Review review) {
                openEditScreen(review);
            }

            @Override
            public void onDelete(Review review) {
                confirmDelete(review);
            }
        });
        binding.reviewList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.reviewList.setAdapter(adapter);

        viewModel.getReviews().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });
    }

    /** Tải lại mỗi khi quay về màn hình này (ví dụ sau khi sửa đánh giá). */
    @Override
    public void onResume() {
        super.onResume();
        viewModel.load(productId);
    }

    private void render(Resource<List<Review>> result) {
        switch (result.status) {
            case LOADING:
                if (adapter.getItemCount() == 0) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                showReviews(result.data);
                break;
            case ERROR:
                binding.summary.setVisibility(View.GONE);
                binding.reviewList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.load(productId));
                break;
        }
    }

    private void showReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            binding.summary.setVisibility(View.GONE);
            binding.reviewList.setVisibility(View.GONE);
            binding.stateView.showEmpty("Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá.", R.drawable.ic_star);
            return;
        }
        binding.stateView.hide();
        binding.reviewList.setVisibility(View.VISIBLE);
        adapter.submit(reviews);

        // Điểm trung bình = tổng số sao / số đánh giá
        double total = 0;
        for (Review review : reviews) {
            total += review.rating;
        }
        double average = total / reviews.size();
        binding.summary.setVisibility(View.VISIBLE);
        binding.averageText.setText(String.format(Locale.US, "%.1f", average));
        binding.averageStars.setRating((float) average);
        binding.countText.setText(reviews.size() + " đánh giá");
    }

    private void openEditScreen(Review review) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_REVIEW_ID, review.id);
        args.putString(Constants.ARG_PRODUCT_ID, productId);
        args.putString(Constants.ARG_PRODUCT_NAME, productName);
        args.putInt(Constants.ARG_RATING, review.rating);
        args.putString(Constants.ARG_COMMENT, review.comment);
        NavHostFragment.findNavController(this).navigate(R.id.writeReviewFragment, args);
    }

    private void confirmDelete(final Review review) {
        ConfirmDialog.show(requireContext(), "Xóa đánh giá?", "Đánh giá này sẽ bị xóa vĩnh viễn.",
                "Xóa", true, () -> viewModel.delete(review));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
