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
 * Quản lý thông tin chi tiết sản phẩm và các thuộc tính mua sắm (Màu, Size, Số lượng).
 */
public class ProductDetailViewModel extends ViewModel {

    /** Đơn vị trạng thái lựa chọn của người mua. */
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

    /** Tải thông tin chi tiết của sản phẩm theo mã định danh. */
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

    /** Xử lý việc chọn hoặc bỏ chọn kích thước sản phẩm. */
    public void selectSize(String size) {
        Product targetProduct = currentProduct();
        Selection currSelection = selection.getValue();
        if (targetProduct == null || currSelection == null) {
            return;
        }
        String updatedSize = size.equals(currSelection.size) ? null : size;
        String color = currSelection.color;
        if (updatedSize != null && color != null && findVariant(targetProduct, updatedSize, color) == null) {
            color = null;
        }
        selection.setValue(build(targetProduct, updatedSize, color, 1));
    }

    /** Xử lý việc chọn hoặc bỏ chọn màu sắc sản phẩm. */
    public void selectColor(String color) {
        Product targetProduct = currentProduct();
        Selection currSelection = selection.getValue();
        if (targetProduct == null || currSelection == null) {
            return;
        }
        String updatedColor = color.equals(currSelection.color) ? null : color;
        String size = currSelection.size;
        if (updatedColor != null && size != null && findVariant(targetProduct, size, updatedColor) == null) {
            size = null;
        }
        selection.setValue(build(targetProduct, size, updatedColor, 1));
    }

    public void setQuantity(int quantity) {
        Selection currSelection = selection.getValue();
        if (currSelection == null) {
            return;
        }
        int validQty = Math.max(1, Math.min(quantity, currSelection.getMaxQuantity()));
        selection.setValue(new Selection(currSelection.size, currSelection.color, validQty, currSelection.variant));
    }

    /** Kiểm tra xem kích cỡ còn sẵn hàng hay không. */
    public boolean isSizeAvailable(Product prod, String sizeVal) {
        Selection currSel = selection.getValue();
        String colorVal = currSel == null ? null : currSel.color;
        for (ProductVariant v : prod.getVariants()) {
            if (sizeVal.equals(v.getSize()) && v.getStockQuantity() > 0
                    && (colorVal == null || colorVal.equals(v.getColor()))) {
                return true;
            }
        }
        return false;
    }

    /** Kiểm tra xem màu sắc còn sẵn hàng hay không. */
    public boolean isColorAvailable(Product prod, String colorVal) {
        Selection currSel = selection.getValue();
        String sizeVal = currSel == null ? null : currSel.size;
        for (ProductVariant v : prod.getVariants()) {
            if (colorVal.equals(v.getColor()) && v.getStockQuantity() > 0
                    && (sizeVal == null || sizeVal.equals(v.getSize()))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Product currentProduct() {
        Resource<Product> res = product.getValue();
        return res == null ? null : res.data;
    }

    private Selection build(Product prod, @Nullable String size, @Nullable String color, int quantity) {
        ProductVariant variant = (size != null && color != null) ? findVariant(prod, size, color) : null;
        return new Selection(size, color, quantity, variant);
    }

    @Nullable
    private ProductVariant findVariant(Product prod, String size, String color) {
        for (ProductVariant var : prod.getVariants()) {
            if (size.equals(var.getSize()) && color.equals(var.getColor())) {
                return var;
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
