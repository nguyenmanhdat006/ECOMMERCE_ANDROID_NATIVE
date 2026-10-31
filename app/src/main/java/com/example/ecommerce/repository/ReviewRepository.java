package com.example.ecommerce.repository;

import com.example.ecommerce.model.Review;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

import java.util.List;

/** Đánh giá sản phẩm. */
public class ReviewRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public void getProductReviews(String productId, RepositoryCallback<List<Review>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.getReviews(productId));
            return;
        }
        enqueue(api.getProductReviews(productId), callback);
    }

    /** Viết đánh giá mới cho một dòng đơn hàng đã mua. */
    public void createReview(Review.Request request, RepositoryCallback<Review> callback) {
        if (Constants.USE_MOCK_DATA) {
            String[] error = new String[1]; // nơi nhận câu báo lỗi (nếu có)
            Review review = MockStore.createReview(request, error);
            if (review == null) {
                MockDataSource.error(callback, error[0]);
            } else {
                MockDataSource.success(callback, review);
            }
            return;
        }
        enqueue(api.createReview(request), callback);
    }

    public void updateReview(String reviewId, Review.Request request, RepositoryCallback<Review> callback) {
        if (Constants.USE_MOCK_DATA) {
            Review review = MockStore.updateReview(reviewId, request);
            if (review == null) {
                MockDataSource.error(callback, "Không tìm thấy dữ liệu.");
            } else {
                MockDataSource.success(callback, review);
            }
            return;
        }
        enqueue(api.updateReview(reviewId, request), callback);
    }

    public void deleteReview(String reviewId, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockStore.deleteReview(reviewId);
            MockDataSource.success(callback, null);
            return;
        }
        enqueue(api.deleteReview(reviewId), callback);
    }
}
