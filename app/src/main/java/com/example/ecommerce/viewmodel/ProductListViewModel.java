package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Giữ trạng thái màn hình danh sách sản phẩm: từ khóa, danh mục, sắp xếp, phân trang.
 * Fragment chỉ quan sát các LiveData và gọi hàm ở đây, không chứa logic tải dữ liệu.
 */
public class ProductListViewModel extends ViewModel {

    private final ProductRepository repository = new ProductRepository();

    private final MutableLiveData<Resource<List<Product>>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);

    private final List<Product> items = new ArrayList<>();
    private String keyword;
    private String categoryId;
    private String sortBy = "name";
    private String sortDirection = "asc";
    private int page = 0;
    private boolean hasNext = false;
    private boolean loading = false;
    private Call<ProductPage> currentCall;

    public ProductListViewModel() {
        loadCategories();
        refresh();
    }

    public LiveData<Resource<List<Product>>> getProducts() { return products; }

    public LiveData<List<Category>> getCategories() { return categories; }

    public LiveData<Boolean> getLoadingMore() { return loadingMore; }

    public String getCategoryId() { return categoryId; }

    public String getSortBy() { return sortBy; }

    public String getSortDirection() { return sortDirection; }

    /** Tải lại từ trang đầu (kéo để làm mới, đổi bộ lọc, bấm Thử lại). */
    public void refresh() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        List<Category> loadedCategories = categories.getValue();
        if (loadedCategories == null || loadedCategories.isEmpty()) {
            loadCategories(); // lần tải trước có thể đã lỗi (mất mạng, backend chưa chạy)
        }
        items.clear();
        hasNext = false;
        loading = false;
        products.setValue(Resource.loading());
        fetch(0);
    }

    /** Gọi khi cuộn tới cuối danh sách. */
    public void loadMore() {
        if (loading || !hasNext) {
            return;
        }
        loadingMore.setValue(true);
        fetch(page + 1);
    }

    public void setKeyword(String newKeyword) {
        String value = (newKeyword == null || newKeyword.trim().isEmpty()) ? null : newKeyword.trim();
        if ((value == null && keyword == null) || (value != null && value.equals(keyword))) {
            return;
        }
        keyword = value;
        refresh();
    }

    public void setCategory(String newCategoryId) {
        if ((newCategoryId == null && categoryId == null)
                || (newCategoryId != null && newCategoryId.equals(categoryId))) {
            return;
        }
        categoryId = newCategoryId;
        refresh();
    }

    public void setSort(String newSortBy, String newSortDirection) {
        sortBy = newSortBy;
        sortDirection = newSortDirection;
        refresh();
    }

    private void loadCategories() {
        repository.getCategories(new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(data);
            }

            @Override
            public void onError(String message) {
                // Không có danh mục vẫn xem được sản phẩm; chip "Tất cả" luôn có.
            }
        });
    }

    private void fetch(int pageToLoad) {
        loading = true;
        currentCall = repository.search(keyword, categoryId, sortBy, sortDirection, pageToLoad,
                Constants.PAGE_SIZE, new RepositoryCallback<ProductPage>() {
                    @Override
                    public void onSuccess(ProductPage data) {
                        page = pageToLoad;
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
                            products.setValue(Resource.error(message));
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
