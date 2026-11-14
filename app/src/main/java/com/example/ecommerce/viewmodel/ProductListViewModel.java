package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductFilter;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Quản lý trạng thái phân trang, tìm kiếm và áp dụng bộ lọc cho danh sách sản phẩm.
 */
public class ProductListViewModel extends ViewModel {

    private final ProductRepository repository = new ProductRepository();

    private final MutableLiveData<Resource<List<Product>>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<Category>());
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);

    private final List<Product> items = new ArrayList<>();
    private ProductFilter filter = new ProductFilter();
    private int page = 0;
    private boolean hasNext = false;
    private boolean loading = false;
    private boolean started = false;
    private Call<ProductPage> currentCall;

    public LiveData<Resource<List<Product>>> getProducts() { return products; }

    public LiveData<List<Category>> getCategories() { return categories; }

    public LiveData<Boolean> getLoadingMore() { return loadingMore; }

    public ProductFilter getFilter() { return filter; }

    /** Khởi động phiên làm việc với các tham số ban đầu. */
    public void start(String keyword, String categoryId) {
        if (started) {
            return;
        }
        started = true;
        filter.keyword = keyword;
        filter.categoryId = categoryId;
        loadCategories();
        refresh();
    }

    /** Làm mới danh sách và quay về trang đầu tiên. */
    public void refresh() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        List<Category> loadedCategories = categories.getValue();
        if (loadedCategories == null || loadedCategories.isEmpty()) {
            loadCategories();
        }
        items.clear();
        hasNext = false;
        loading = false;
        products.setValue(Resource.<List<Product>>loading());
        fetch(0);
    }

    /** Tải tiếp trang dữ liệu kế tiếp khi cuộn danh sách. */
    public void loadMore() {
        if (loading || !hasNext) {
            return;
        }
        loadingMore.setValue(true);
        fetch(page + 1);
    }

    public void setKeyword(String newKeyword) {
        String trimmedKeyword = (newKeyword == null || newKeyword.trim().isEmpty()) ? null : newKeyword.trim();
        if (isSame(trimmedKeyword, filter.keyword)) {
            return;
        }
        filter.keyword = trimmedKeyword;
        refresh();
    }

    public void setCategory(String newCategoryId) {
        if (isSame(newCategoryId, filter.categoryId)) {
            return;
        }
        filter.categoryId = newCategoryId;
        refresh();
    }

    public void setSort(String sortBy, String sortDirection) {
        filter.sortBy = sortBy;
        filter.sortDirection = sortDirection;
        refresh();
    }

    /** Cập nhật bộ lọc nâng cao. */
    public void applyAdvancedFilter(ProductFilter advanced) {
        filter.brand = advanced.brand;
        filter.minPrice = advanced.minPrice;
        filter.maxPrice = advanced.maxPrice;
        filter.minRating = advanced.minRating;
        filter.onlyNew = advanced.onlyNew;
        refresh();
    }

    private boolean isSame(String firstStr, String secondStr) {
        if (firstStr == null) {
            return secondStr == null;
        }
        return firstStr.equals(secondStr);
    }

    private void loadCategories() {
        repository.getCategories(new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(data);
            }

            @Override
            public void onError(String message) {
            }
        });
    }

    private void fetch(final int targetPage) {
        loading = true;
        currentCall = repository.search(filter, targetPage, Constants.PAGE_SIZE,
                new RepositoryCallback<ProductPage>() {
                    @Override
                    public void onSuccess(ProductPage data) {
                        page = targetPage;
                        hasNext = data.hasNext();
                        items.addAll(data.getProducts());
                        loading = false;
                        loadingMore.setValue(false);
                        products.setValue(Resource.success(new ArrayList<>(items)));
                    }

                    @Override
                    public void onError(String message) {
                        loading = false;
                        loadingMore.setValue(false);
                        if (items.isEmpty()) {
                            products.setValue(Resource.<List<Product>>error(message));
                        }
                    }
                });
    }

    @Override
    protected void onCleared() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
