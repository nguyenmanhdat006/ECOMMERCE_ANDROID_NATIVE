package com.example.ecommerce.repository;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductFilter;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

import java.util.List;

import retrofit2.Call;

/**
 * Lấy dữ liệu sản phẩm và danh mục. ViewModel chỉ gọi repository, không cần biết dữ liệu đến từ đâu.
 *
 * Mỗi hàm trả về Call (yêu cầu mạng) để ViewModel có thể hủy khi có yêu cầu mới thay thế.
 * Khi dùng dữ liệu giả (Constants.USE_MOCK_DATA = true) thì không có Call nên trả về null.
 */
public class ProductRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public Call<ProductPage> search(ProductFilter filter, int page, int size,
                                    RepositoryCallback<ProductPage> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.search(filter, page, size, callback);
            return null;
        }
        // isNewArrival chỉ gửi khi bật lọc "hàng mới" (null thì không gửi)
        Boolean isNewArrival = filter.onlyNew ? Boolean.TRUE : null;
        Call<ProductPage> call = api.searchProducts(filter.keyword, filter.categoryId, filter.brand,
                filter.minPrice, filter.maxPrice, filter.minRating, isNewArrival,
                filter.sortBy, filter.sortDirection, page, size);
        return enqueue(call, callback);
    }

    public Call<Product> getProduct(String id, RepositoryCallback<Product> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.getProduct(id, callback);
            return null;
        }
        return enqueue(api.getProduct(id), callback);
    }

    public Call<List<Category>> getCategories(RepositoryCallback<List<Category>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.getCategories(callback);
            return null;
        }
        return enqueue(api.getCategories(), callback);
    }
}
