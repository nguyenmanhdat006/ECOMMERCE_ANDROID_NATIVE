package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Review;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.repository.ReviewRepository;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Quản lý các tương tác gửi bài, chỉnh sửa, xóa bài đánh giá sản phẩm.
 */
public class ReviewViewModel extends ViewModel {

    private final ReviewRepository repository = new ReviewRepository();

    private final MutableLiveData<Resource<List<Review>>> reviews = new MutableLiveData<>();
    private final MutableLiveData<Resource<Review>> submitResult = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    private String productId;

    public LiveData<Resource<List<Review>>> getReviews() { return reviews; }

    public LiveData<Resource<Review>> getSubmitResult() { return submitResult; }

    public LiveData<String> getMessage() { return message; }

    public void clearSubmitResult() { submitResult.setValue(null); }

    public void clearMessage() { message.setValue(null); }

    public void load(String productId) {
        this.productId = productId;
        reviews.setValue(Resource.<List<Review>>loading());
        fetch();
    }

    /** Tạo bài đánh giá mới hoặc cập nhật nội dung phản hồi cũ. */
    public void submit(String reviewId, Review.Request request) {
        submitResult.setValue(Resource.<Review>loading());
        RepositoryCallback<Review> responseCb = new RepositoryCallback<Review>() {
            @Override
            public void onSuccess(Review data) {
                submitResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                submitResult.setValue(Resource.<Review>error(errorMessage));
            }
        };
        if (reviewId == null) {
            repository.createReview(request, responseCb);
        } else {
            repository.updateReview(reviewId, request, responseCb);
        }
    }

    public void delete(Review review) {
        repository.deleteReview(review.id, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetch();
            }

            @Override
            public void onError(String errorMessage) {
                message.setValue(errorMessage);
            }
        });
    }

    private void fetch() {
        repository.getProductReviews(productId, new RepositoryCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> data) {
                reviews.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                reviews.setValue(Resource.<List<Review>>error(errorMessage));
            }
        });
    }
}
