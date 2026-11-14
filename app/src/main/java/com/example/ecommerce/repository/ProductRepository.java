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
 * Xử lý truy vấn dữ liệu sản phẩm và phân loại danh mục.
 */
public class ProductRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public Call<ProductPage> search(ProductFilter filter, int page, int size,
                                    RepositoryCallback<ProductPage> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.search(filter, page, size, callback);
            return null;
        }
        Boolean flagNewArrival = filter.onlyNew ? Boolean.TRUE : null;
        Call<ProductPage> apiCall = api.searchProducts(filter.keyword, filter.categoryId, filter.brand,
                filter.minPrice, filter.maxPrice, filter.minRating, flagNewArrival,
                filter.sortBy, filter.sortDirection, page, size);
        return enqueue(apiCall, callback);
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
