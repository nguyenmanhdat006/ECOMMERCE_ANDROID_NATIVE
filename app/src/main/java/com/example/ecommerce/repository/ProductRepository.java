package com.example.ecommerce.repository;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

import java.util.List;

import retrofit2.Call;

/**
 * Lớp truy cập dữ liệu sản phẩm. ViewModel chỉ gọi repository, không biết dữ liệu đến từ đâu.
 * Mỗi hàm trả về Call để ViewModel có thể hủy khi có yêu cầu mới thay thế.
 *
 * Khi Constants.USE_MOCK_DATA = true, dữ liệu lấy từ MockDataSource (không cần backend) và các hàm trả về null
 * (không có Call để hủy; kết quả của yêu cầu cũ bị bỏ nếu đã có yêu cầu mới hơn).
 */
public class ProductRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    // Số thứ tự yêu cầu mới nhất của từng loại, dùng để bỏ kết quả mock đã cũ
    private static int searchSeq;
    private static int productSeq;
    private static int categorySeq;

    public Call<ProductPage> search(String keyword, String categoryId, String sortBy, String sortDirection,
                                    int page, int size, RepositoryCallback<ProductPage> callback) {
        if (Constants.USE_MOCK_DATA) {
            int seq = ++searchSeq;
            MockDataSource.search(keyword, categoryId, sortBy, sortDirection, page, size,
                    latestOnly(callback, () -> seq == searchSeq));
            return null;
        }
        return enqueue(api.searchProducts(keyword, categoryId, sortBy, sortDirection, page, size), callback);
    }

    public Call<Product> getProduct(String id, RepositoryCallback<Product> callback) {
        if (Constants.USE_MOCK_DATA) {
            int seq = ++productSeq;
            MockDataSource.getProduct(id, latestOnly(callback, () -> seq == productSeq));
            return null;
        }
        return enqueue(api.getProduct(id), callback);
    }

    public Call<List<Category>> getCategories(RepositoryCallback<List<Category>> callback) {
        if (Constants.USE_MOCK_DATA) {
            int seq = ++categorySeq;
            MockDataSource.getCategories(latestOnly(callback, () -> seq == categorySeq));
            return null;
        }
        return enqueue(api.getCategories(), callback);
    }

    /** Chỉ chuyển kết quả cho callback khi yêu cầu này vẫn là yêu cầu mới nhất. */
    private static <T> RepositoryCallback<T> latestOnly(RepositoryCallback<T> callback,
                                                        java.util.function.BooleanSupplier isLatest) {
        return new RepositoryCallback<T>() {
            @Override
            public void onSuccess(T data) {
                if (isLatest.getAsBoolean()) {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onError(String message) {
                if (isLatest.getAsBoolean()) {
                    callback.onError(message);
                }
            }
        };
    }
}
