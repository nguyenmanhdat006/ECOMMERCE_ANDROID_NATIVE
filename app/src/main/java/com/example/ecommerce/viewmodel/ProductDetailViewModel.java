package com.example.ecommerce.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductVariant;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import retrofit2.Call;

/**
 * Trạng thái màn hình chi tiết sản phẩm: dữ liệu sản phẩm và lựa chọn size, màu, số lượng.
 */
public class ProductDetailViewModel extends ViewModel {

    /** Lựa chọn hiện tại của người dùng. variant = null khi chưa chọn đủ size và màu. */
    public static class Selection {
        @Nullable public final String size;
        @Nullable public final String color;
        public final int quantity;
        @Nullable public final ProductVariant variant;

        Selection(@Nullable String size, @Nullable String color, int quantity, @Nullable ProductVariant variant) {
            this.size = size;
            this.color = color;
            this.quantity = quantity;
            this.variant = variant;
        }

        public int getMaxQuantity() {
            return variant == null ? 1 : Math.max(1, variant.getStockQuantity());
        }
    }

    private final ProductRepository repository = new ProductRepository();
    private final MutableLiveData<Resource<Product>> product = new MutableLiveData<>();
    private final MutableLiveData<Selection> selection = new MutableLiveData<>(new Selection(null, null, 1, null));

    private Call<Product> currentCall;
    private String loadedId;

    public LiveData<Resource<Product>> getProduct() { return product; }

    public LiveData<Selection> getSelection() { return selection; }

    /** Tải sản phẩm; gọi lại cùng id khi đã tải xong thì bỏ qua. */
    public void load(String productId) {
        Resource<Product> current = product.getValue();
        if (productId.equals(loadedId) && current != null && current.status == Resource.Status.SUCCESS) {
            return;
        }
        loadedId = productId;
        reload();
    }

    public void reload() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        product.setValue(Resource.loading());
        currentCall = repository.getProduct(loadedId, new RepositoryCallback<Product>() {
            @Override
            public void onSuccess(Product data) {
                String size = data.getSizes().size() == 1 ? data.getSizes().get(0) : null;
                String color = data.getColors().size() == 1 ? data.getColors().get(0) : null;
                selection.setValue(build(data, size, color, 1));
                product.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                product.setValue(Resource.error(message));
            }
        });
    }

    /** Bấm lần nữa vào size đang chọn thì bỏ chọn. */
    public void selectSize(String size) {
        Product p = currentProduct();
        Selection s = selection.getValue();
        if (p == null || s == null) {
            return;
        }
        String newSize = size.equals(s.size) ? null : size;
        String color = s.color;
        if (newSize != null && color != null && findVariant(p, newSize, color) == null) {
            color = null;
        }
        selection.setValue(build(p, newSize, color, 1));
    }

    public void selectColor(String color) {
        Product p = currentProduct();
        Selection s = selection.getValue();
        if (p == null || s == null) {
            return;
        }
        String newColor = color.equals(s.color) ? null : color;
        String size = s.size;
        if (newColor != null && size != null && findVariant(p, size, newColor) == null) {
            size = null;
        }
        selection.setValue(build(p, size, newColor, 1));
    }

    public void setQuantity(int quantity) {
        Selection s = selection.getValue();
        if (s == null) {
            return;
        }
        int clamped = Math.max(1, Math.min(quantity, s.getMaxQuantity()));
        selection.setValue(new Selection(s.size, s.color, clamped, s.variant));
    }

    /** Size còn hàng (theo màu đang chọn, nếu có). */
    public boolean isSizeAvailable(Product p, String size) {
        Selection s = selection.getValue();
        String color = s == null ? null : s.color;
        for (ProductVariant v : p.getVariants()) {
            if (size.equals(v.getSize()) && v.getStockQuantity() > 0
                    && (color == null || color.equals(v.getColor()))) {
                return true;
            }
        }
        return false;
    }

    /** Màu còn hàng (theo size đang chọn, nếu có). */
    public boolean isColorAvailable(Product p, String color) {
        Selection s = selection.getValue();
        String size = s == null ? null : s.size;
        for (ProductVariant v : p.getVariants()) {
            if (color.equals(v.getColor()) && v.getStockQuantity() > 0
                    && (size == null || size.equals(v.getSize()))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Product currentProduct() {
        Resource<Product> r = product.getValue();
        return r == null ? null : r.data;
    }

    private Selection build(Product p, @Nullable String size, @Nullable String color, int quantity) {
        ProductVariant variant = (size != null && color != null) ? findVariant(p, size, color) : null;
        return new Selection(size, color, quantity, variant);
    }

    @Nullable
    private ProductVariant findVariant(Product p, String size, String color) {
        for (ProductVariant v : p.getVariants()) {
            if (size.equals(v.getSize()) && color.equals(v.getColor())) {
                return v;
            }
        }
        return null;
    }

    @Override
    protected void onCleared() {
        if (currentCall != null) {
            currentCall.cancel();
        }
    }
}
