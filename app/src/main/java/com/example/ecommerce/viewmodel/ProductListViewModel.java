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
 * Giữ trạng thái danh sách sản phẩm: bộ lọc (từ khóa, danh mục, sắp xếp...) và phân trang.
 * Dùng chung cho Trang chủ và màn Tìm kiếm. Fragment chỉ quan sát LiveData và gọi các hàm ở đây.
 */
public class ProductListViewModel extends ViewModel {

    private final ProductRepository repository = new ProductRepository();

    // Dữ liệu cho giao diện quan sát
    private final MutableLiveData<Resource<List<Product>>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<Category>());
    private final MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);

    // Trạng thái bên trong
    private final List<Product> items = new ArrayList<>(); // các sản phẩm đã tải (gộp mọi trang)
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

    /**
     * Bắt đầu tải lần đầu với từ khóa và danh mục cho trước (có thể null).
     * Chỉ chạy một lần; gọi lại khi xoay màn hình sẽ bị bỏ qua.
     */
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
        products.setValue(Resource.<List<Product>>loading());
        fetch(0);
    }

    /** Gọi khi cuộn tới cuối danh sách để tải trang kế tiếp. */
    public void loadMore() {
        if (loading || !hasNext) {
            return;
        }
        loadingMore.setValue(true);
        fetch(page + 1);
    }

    public void setKeyword(String newKeyword) {
        String value = (newKeyword == null || newKeyword.trim().isEmpty()) ? null : newKeyword.trim();
        if (isSame(value, filter.keyword)) {
            return;
        }
        filter.keyword = value;
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

    /** Áp dụng các bộ lọc nâng cao (giá, thương hiệu, đánh giá, hàng mới) từ bảng Lọc. */
    public void applyAdvancedFilter(ProductFilter advanced) {
        filter.brand = advanced.brand;
        filter.minPrice = advanced.minPrice;
        filter.maxPrice = advanced.maxPrice;
        filter.minRating = advanced.minRating;
        filter.onlyNew = advanced.onlyNew;
        refresh();
    }

    /** So sánh hai chuỗi có thể null. */
    private boolean isSame(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
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

    /** Tải một trang sản phẩm rồi nối vào danh sách đã có. */
    private void fetch(final int pageToLoad) {
        loading = true;
        currentCall = repository.search(filter, pageToLoad, Constants.PAGE_SIZE,
                new RepositoryCallback<ProductPage>() {
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
                            products.setValue(Resource.<List<Product>>error(message));
                        }
                        // Nếu đã có sản phẩm hiển thị thì giữ nguyên, lần cuộn sau sẽ thử tải lại
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
