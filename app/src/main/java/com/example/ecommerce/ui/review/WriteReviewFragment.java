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

import com.example.ecommerce.databinding.FragmentWriteReviewBinding;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.ReviewViewModel;

/**
 * Màn hình viết đánh giá mới hoặc sửa đánh giá cũ.
 *  - Viết mới: cần productId, orderItemId (dòng đơn đã mua).
 *  - Sửa: có thêm reviewId, rating, comment hiện tại.
 */
public class WriteReviewFragment extends Fragment {

    private static final String[] RATING_LABELS = {"", "Rất tệ", "Tệ", "Bình thường", "Tốt", "Rất tốt"};

    private FragmentWriteReviewBinding binding;
    private ReviewViewModel viewModel;

    private String reviewId;      // null nghĩa là đang viết mới
    private String productId;
    private String orderItemId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWriteReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ReviewViewModel.class);
        readArguments();

        // Sao có thể bấm để chọn
        binding.ratingStars.setStarSize(40);
        binding.ratingStars.setEditable(true, rating -> binding.ratingLabel.setText(RATING_LABELS[rating]));

        binding.submitButton.setOnClickListener(v -> submit());
        viewModel.getSubmitResult().observe(getViewLifecycleOwner(), this::render);
    }

    /** Đọc dữ liệu màn hình trước truyền sang và hiển thị. */
    private void readArguments() {
        Bundle args = requireArguments();
        reviewId = args.getString(Constants.ARG_REVIEW_ID);
        productId = args.getString(Constants.ARG_PRODUCT_ID);
        orderItemId = args.getString(Constants.ARG_ORDER_ITEM_ID);

        binding.productLine.bind(args.getString(Constants.ARG_IMAGE_URL),
                args.getString(Constants.ARG_PRODUCT_NAME), null, "");

        if (reviewId != null) {
            // Đang sửa: điền sẵn nội dung cũ
            binding.toolbar.setTitle("Sửa đánh giá");
            binding.submitButton.setText("Lưu thay đổi");
            int rating = args.getInt(Constants.ARG_RATING, 0);
            binding.ratingStars.setRating(rating);
            binding.ratingLabel.setText(RATING_LABELS[rating]);
            binding.commentInput.setText(args.getString(Constants.ARG_COMMENT));
        }
    }

    private void submit() {
        int rating = binding.ratingStars.getRating();
        if (rating == 0) {
            binding.errorText.setText("Hãy chọn số sao.");
            binding.errorText.setVisibility(View.VISIBLE);
            return;
        }
        Review.Request request = new Review.Request();
        request.rating = rating;
        request.comment = binding.commentInput.getText().toString().trim();
        if (reviewId == null) {
            // Chỉ khi viết mới mới cần biết sản phẩm và dòng đơn đã mua
            request.productId = productId;
            request.orderItemId = orderItemId;
        }
        viewModel.submit(reviewId, request);
    }

    private void render(Resource<Review> result) {
        if (result == null) {
            return;
        }
        switch (result.status) {
            case LOADING:
                binding.submitButton.setLoading(true);
                binding.errorText.setVisibility(View.GONE);
                break;
            case ERROR:
                binding.submitButton.setLoading(false);
                binding.errorText.setText(result.message);
                binding.errorText.setVisibility(View.VISIBLE);
                viewModel.clearSubmitResult();
                break;
            case SUCCESS:
                binding.submitButton.setLoading(false);
                viewModel.clearSubmitResult();
                Toast.makeText(requireContext(), "Cảm ơn bạn đã đánh giá", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).popBackStack();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
