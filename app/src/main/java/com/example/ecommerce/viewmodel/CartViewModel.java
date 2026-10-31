package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Giỏ hàng: tải danh sách, đổi số lượng, xóa dòng, thêm sản phẩm.
 * Dùng ở màn Giỏ hàng, Chi tiết sản phẩm (nút thêm giỏ) và Trang chủ (số trên biểu tượng giỏ).
 */
public class CartViewModel extends ViewModel {

    private final CartRepository repository = new CartRepository();

    private final MutableLiveData<Resource<List<CartItem>>> cart = new MutableLiveData<>();

    /** Thông báo ngắn cho người dùng (thêm giỏ thành công, lỗi vượt tồn kho...). Hiển thị xong thì gọi clearMessage(). */
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<Resource<List<CartItem>>> getCart() { return cart; }

    public LiveData<String> getMessage() { return message; }

    public void clearMessage() { message.setValue(null); }

    /** Tải giỏ hàng và hiện trạng thái đang tải. */
    public void load() {
        cart.setValue(Resource.<List<CartItem>>loading());
        fetchCart();
    }

    /** Tải lại giỏ hàng nhưng không hiện vòng quay (dùng sau khi sửa số lượng, xóa). */
    public void reloadQuietly() {
        fetchCart();
    }

    public void changeQuantity(CartItem item, int newQuantity) {
        repository.updateQuantity(item.id, item.productVariant.getId(), newQuantity, new ActionCallback(null));
    }

    public void remove(CartItem item) {
        repository.deleteItem(item.id, new ActionCallback(null));
    }

    /** Thêm vào giỏ; thành công thì báo "Đã thêm vào giỏ hàng". */
    public void addToCart(String variantId, int quantity) {
        repository.addToCart(variantId, quantity, new ActionCallback("Đã thêm vào giỏ hàng"));
    }

    private void fetchCart() {
        repository.getCart(new RepositoryCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cart.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                cart.setValue(Resource.<List<CartItem>>error(errorMessage));
            }
        });
    }

    /**
     * Xử lý kết quả của một thao tác sửa giỏ hàng: báo cho người dùng rồi tải lại giỏ.
     * successMessage = null thì không báo khi thành công.
     */
    private class ActionCallback implements RepositoryCallback<Void> {
        private final String successMessage;

        ActionCallback(String successMessage) {
            this.successMessage = successMessage;
        }

        @Override
        public void onSuccess(Void data) {
            if (successMessage != null) {
                message.setValue(successMessage);
            }
            reloadQuietly();
        }

        @Override
        public void onError(String errorMessage) {
            message.setValue(errorMessage);
            reloadQuietly();
        }
    }
}
